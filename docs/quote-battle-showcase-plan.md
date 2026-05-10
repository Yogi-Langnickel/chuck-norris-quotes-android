# Quote Battle Showcase Plan

Status: implemented for v1.2.1 showcase

## Goal

Make Quote Battle presentable for a morning demo on May 12, 2026 by turning the app into a battle-first experience with dedicated fact tabs.

## Implemented Slice

- A battle round loads Chuck and Cat contenders without changing score totals.
- The user swipes away the contender they do not want; the remaining contender wins the point.
- Daily, weekly, and monthly local scores update once per chosen round.
- The refreshed challenger slides in from the opposite side of the swipe.
- Tie break refreshes both Chuck and Cat without awarding a point.
- Battle cards no longer show duplicate labels or power scores.
- Battle Mode, Chuck Facts, and Cat Facts are split into separate tabs.
- Each fact stream is limited to 10 requests per minute.
- The app has a latest-release link in the top bar for easier manual updates.

## Showcase Notes

- Scores are local to the device and persist through app restarts.
- There is no backend leaderboard yet.
- The update action opens the GitHub latest-release page; it is not a silent in-app updater.
- Installing an APK with the same package name and signing key updates the existing app and should keep local scores.
- If Android reports a signature mismatch, uninstall the previous app first; uninstalling clears local scores.

## Later Enhancements

- Chaos champion streak.
- Standalone battle-first mode.
- Shareable battle result card.
- Daily challenge prompt.
