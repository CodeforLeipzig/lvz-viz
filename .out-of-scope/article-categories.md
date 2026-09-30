# Article categories

lvz-viz does not assign categories (robbery, accident, burglary, …) to police ticker articles, and it does not colour map pins by category.

## Why this is out of scope

The LVZ police ticker has no category metadata, so every category would have to be inferred from free German text.
That needs a fixed taxonomy plus either a hand-maintained keyword list or a trained classifier, and both have to be kept in sync with how the LVZ writes its articles.
Wrong categories are worse than none on a map about crime: a pin coloured "robbery" that is actually a traffic accident misinforms readers.

The existing full-text search already covers the practical need.
Searching for `raubüberfall`, `unfall` or `einbruch` shows the matching articles on the map and in the list, without a classification step that could be wrong.

## Prior requests

- #7: "create categories for the articles"
