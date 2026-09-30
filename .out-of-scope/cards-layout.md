# Cards layout for search results

The search view shows results as a paginated table with expandable rows, not as a grid of cards.

## Why this is out of scope

The concern behind the request was endless scrolling.
The current view already avoids that: results are paginated (5 per page) next to the map, and each row expands in place to show the article text, author and a link to the original article.

A card grid would compete with the map for space in the split view, and on small screens the table rows stack more compactly than cards.
Switching layouts would be a visual preference rather than a functional improvement.

## Prior requests

- #14: "Cards layout"
