# API Sources

## Active

- Chuck facts: `https://api.chucknorris.io/jokes/random`
- Cat facts: `https://catfact.ninja/fact`
- Cat facts alternate: `https://meowfacts.herokuapp.com/`
- Dog facts primary: `https://dogapi.dog/api/v2/facts?limit=1`
- Dog facts fallback: `https://dog-api.kinduff.com/api/facts`
- Ask Yogi rotation: `https://api.adviceslip.com/advice`
- Ask Yogi rotation: `https://icanhazdadjoke.com/`

Cat facts rotate between Cat Fact Ninja and MeowFacts. Dog facts try Dog API
v2 first, then the Kinduff classic endpoint as a bounded fallback so Dog and
Battle do not hang on a single provider outage. If both remote dog providers
fail, the app uses a small local emergency dog-fact fallback so Dog and Battle
remain usable during provider outages. Battle challengers are selected from a
random eligible fact stream that excludes the current winner, with fallback to
another eligible stream when a source fails. Ask Yogi rotates across third-party
advice and joke endpoints with per-provider timeouts, skips blank or malformed
payloads, and returns a short local API-error message if every provider fails.
I Can Haz Dad Joke requires `Accept: application/json`.
Chuck facts stay on
`api.chucknorris.io` because the investigated alternatives were either API-key
based or unreliable.

Dog API terms require visible attribution. The app shows: "Powered by
Stratonauts Dog API".

Ask Yogi content is untrusted third-party text. Keep remote responses rendered as
plain text, with blank/malformed/oversized payloads rejected before display.

## Investigated

- API Ninjas Chuck Norris: available, but requires an `X-Api-Key`.
- ICNDB: legacy endpoint; live checks did not return a usable HTTPS JSON response.
- Cat Facts Heroku API: documented publicly, but live checks returned a Heroku application error.
- Kanye REST: removed from Ask Yogi rotation by product decision.
- Ron Swanson Quotes: removed from Ask Yogi rotation by product decision.
- Evil Insult: removed from Ask Yogi rotation because it was repetitive and carries content-rating risk.
