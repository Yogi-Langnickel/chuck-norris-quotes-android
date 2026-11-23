#!/usr/bin/env python3
"""Sign a reviewed Quote Battle release APK on an authorized signing host."""

import argparse
import hashlib
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile


PACKAGE = "com.yogi.quotebattleroyal"


def sha256_argument(value):
    if not re.fullmatch(r"[0-9a-fA-F]{64}", value):
        raise argparse.ArgumentTypeError("expected a 64-digit SHA-256 digest")
    return value.lower()


def sha256_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apk", required=True, type=Path, help="approved unsigned release APK")
    parser.add_argument("--apk-sha256", required=True, type=sha256_argument)
    parser.add_argument("--signer-sha256", required=True, type=sha256_argument)
    parser.add_argument("--keystore", required=True, type=Path)
    parser.add_argument("--alias", required=True)
    parser.add_argument("--out", required=True, type=Path, help="new signed APK path")
    parser.add_argument("--build-tools", required=True, type=Path, help="trusted Android SDK build-tools directory")
    args = parser.parse_args()

    repo = Path(__file__).resolve().parents[1]
    keystore = args.keystore.resolve(strict=True)
    if not keystore.is_file() or repo in keystore.parents:
        parser.error("use a keystore stored outside the repository")
    if not args.apk.is_file():
        parser.error("select an existing release APK file")
    if args.out.suffix != ".apk" or os.path.lexists(args.out):
        parser.error("select a new .apk output path")

    build_tools = args.build_tools.resolve(strict=True)
    apksigner = build_tools / "apksigner"
    aapt2 = build_tools / "aapt2"
    with tempfile.TemporaryDirectory(prefix=".quote-battle-sign-", dir=args.out.parent) as temporary:
        selected = Path(temporary) / "selected.apk"
        signed = Path(temporary) / "signed.apk"
        shutil.copyfile(args.apk, selected)
        if sha256_file(selected) != args.apk_sha256:
            parser.error("the selected APK does not match the approved SHA-256")

        badging = subprocess.check_output([aapt2, "dump", "badging", selected], text=True).splitlines()
        if not badging or not badging[0].startswith(f"package: name='{PACKAGE}' "):
            parser.error("the selected APK has an unexpected application ID")
        if any(line.startswith("application-debuggable") for line in badging):
            parser.error("the selected APK is debuggable")

        verify = [apksigner, "verify", "--print-certs"]
        unsigned = subprocess.run([*verify, selected], capture_output=True)
        if unsigned.returncode == 0:
            parser.error("the selected APK already has a valid signature")

        # apksigner prompts for passwords on stdin. They never enter Gradle or argv.
        subprocess.run(
            [apksigner, "sign", "--ks", keystore, "--ks-key-alias", args.alias, "--out", signed, selected],
            check=True,
        )
        certificates = re.findall(
            r"^Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})$",
            subprocess.check_output([*verify, signed], text=True),
            re.MULTILINE,
        )
        if [certificate.lower() for certificate in certificates] != [args.signer_sha256]:
            parser.error("the signed APK does not have the approved signer certificate")

        signed_digest = sha256_file(signed)
        # Publish only a verified output, and fail atomically if the name was taken.
        os.link(signed, args.out)

    print(f"Signed APK SHA-256: {signed_digest}")


if __name__ == "__main__":
    try:
        main()
    except (OSError, subprocess.CalledProcessError) as error:
        raise SystemExit(f"Release signing failed: {error}") from error
