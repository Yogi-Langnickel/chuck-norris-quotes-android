# Quote Battle Showcase Plan

Status: implemented for v1.1.0 showcase

## Goal

Make Quote Battle presentable for a morning demo on May 12, 2026 by making the user choose the winner before scores are recorded.

## Implemented Slice

- A battle round loads Chuck and Cat contenders without changing score totals.
- Power scores remain visible as entertainment stats only.
- The user chooses Chuck or Cat as the winner.
- Daily, weekly, and monthly local scores update once per chosen round.
- The selected winner becomes the featured quote/fact.
- Rematch resets the choice and allows the next round to be scored.

## Showcase Notes

- Scores are local to the device and persist through app restarts.
- There is no backend leaderboard yet.
- Installing an APK with the same package name and signing key updates the existing app and should keep local scores.
- If Android reports a signature mismatch, uninstall the previous app first; uninstalling clears local scores.

## Later Enhancements

- Chaos champion streak.
- Standalone battle-first mode.
- Shareable battle result card.
- Daily challenge prompt.
