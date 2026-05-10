# API Sources

## Active

- Chuck facts: `https://api.chucknorris.io/jokes/random`
- Cat facts: `https://catfact.ninja/fact`
- Cat facts alternate: `https://meowfacts.herokuapp.com/`

Cat facts rotate between Cat Fact Ninja and MeowFacts. Chuck facts stay on `api.chucknorris.io` because the investigated alternatives were either API-key based or unreliable.

## Investigated

- API Ninjas Chuck Norris: available, but requires an `X-Api-Key`.
- ICNDB: legacy endpoint; live checks did not return a usable HTTPS JSON response.
- Cat Facts Heroku API: documented publicly, but live checks returned a Heroku application error.
