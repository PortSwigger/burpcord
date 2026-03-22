# Upstream plan: Montoya API — efficient site map size (not executed)

This is a **template for a GitHub issue or discussion** on [PortSwigger/burp-extensions-montoya-api](https://github.com/PortSwigger/burp-extensions-montoya-api). It is **not** an opened PR or issue until you submit it.

## Problem

`SiteMap.requestResponses()` returns `List<HttpRequestResponse>` of **all** site map items. Extensions that only need a **count** must allocate that entire list, which is problematic for **very large projects** (BApp Store criterion 9).

Burpcord v2.6.0 works around this with a bounded proxy-side unique-URL set plus a **rare** full `requestResponses().size()` on a background thread.

## Suggested upstream enhancement (pick one or combine)

1. **`int requestResponseCount()`** (or `long`) — O(1) or internal-count style, no list materialization.  
2. **`Stream<HttpRequestResponse> requestResponsesStream()`** — lazy iteration so callers can `count()` or short-circuit without holding all elements in memory at once (exact API shape up to PortSwigger).  
3. **Callback / visitor API** — e.g. walk site map nodes and increment a counter without returning a giant list.

## Issue draft (copy-paste)

**Title:** Feature request: efficient site map size / lazy access without `requestResponses()` materializing full list

**Body:**

- **Context:** BApp Store reviewers flag extensions that call `siteMap().requestResponses().size()` on a timer because it loads the entire site map into memory.  
- **Ask:** Expose a way to obtain the current site map **item count** (or iterate lazily) without building a full `List<HttpRequestResponse>`.  
- **Reference:** Current `SiteMap` Javadoc only documents `requestResponses()` and filtered variants returning `List`.

## After filing

- Link the issue from [bapp-store-reviewer-response.md](../bapp-store-reviewer-response.md) or your BApp resubmission notes.  
- If the API is added, Burpcord can simplify `BurpcordSiteMapProvider` by using the new method and reducing reliance on periodic full scans.
