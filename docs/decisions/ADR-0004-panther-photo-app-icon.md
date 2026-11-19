# ADR-0004: Owner-supplied panther artwork as the application icon

Status: accepted · Date: 2026-11-19

## Context

The project shipped a hand-drawn vector identity: an `#22C55E` dumbbell on a `#101820`/`#182532`
gradient disc for the launcher, a white dwarf glyph for themed icons, and a 32 dp dumbbell crest in
`PantherBrandHeader`. On 2026-11-19 the owner supplied a single photograph ("Fierce Panther in the
Shadows", 1254 × 1254 PNG, near-black background, glowing green eyes) and asked for it to become the
application icon **everywhere**, with the empty space around the artwork respected
("فضای خالی اطراف آیکون رو رعایت کن") so launcher masks never clip the ears, whiskers or jaw.

Three constraints shape the answer:

1. Adaptive icons on API 26+ render a 108 dp layer, but the launcher only shows the central 72 dp and
   then applies an arbitrary mask (circle, squircle, rounded square, and — on some OEM themes —
   larger shapes). Google's guidance keeps meaningful content inside a 66 dp safe circle.
2. The photograph is a photograph: its black background is `srgb(1,2,2)`, not identical to any chosen
   canvas colour. Pasting the unmodified rectangle produced a visible lighter square seam in the
   first attempt, so the artwork has to fade into the canvas rather than sit on it.
3. `android:foreground` on an `<adaptive-icon>` accepts any drawable, and a per-density PNG is a
   drawable; a vector could not carry the photograph.

## Options

| option | pros | cons | risk |
|---|---|---|---|
| Recolour the existing vector identity in the photo's palette | tiny APK, crisp at every size, themed layer unchanged | not the owner's image; the request was explicitly "this photo" | high (rejected by the owner's wording) |
| Paste the trimmed photo on a solid canvas (`48/108`) | straightforward | visible seam around the pasted rectangle at 100% zoom | high (tried and rejected) |
| Photo as a feathered foreground bitmap at `60/108`, canvas colour taken from the photo's own corners, plus legacy/round rasters and a luminance-derived monochrome layer | one source image everywhere, no seam, safe-zone respected | ~430 KB of PNGs in the APK, artwork is not vector | low |

## Decision

Use the supplied photo as the single icon source, processed at build time (ImageMagick, offline) and
committed as rasters:

- **Canvas**: `#010202`, the colour measured in the photo's corners, so background and artwork are
  indistinguishable.
- **Subject size**: `60/108 dp` of the adaptive canvas (240 px of the 432 px `xxxhdpi` layer). The
  artwork's luminous content measures 201 × 201 px centred, i.e. a 100.5 px radius against the
  132 px safe-circle radius — a ~24% margin.
- **Seam**: the outer 9% of the pasted square gets an alpha feather (a blurred white rectangle as the
  `CopyOpacity` source) before it is composited on the canvas, so the photo dissolves into the
  background instead of ending on a hard edge.
- **Legacy and round icons** (`mipmap-*dpi/ic_launcher.png`, `ic_launcher_round.png`): the same
  artwork at `84/108` of the canvas, the round variant carrying a circular alpha mask.
- **Themed icons** (`drawable-*dpi/ic_launcher_monochrome.png`): Android tints a single-colour layer
  and composites it on a system-coloured background, so only solid shapes survive. Two candidates were
  rendered at real size: (a) the photo's luminance as the alpha channel, which turned to grey haze on a
  light background and to an unreadable blob on a dark one, and (b) a filled silhouette — the 5%
  threshold of a slightly blurred copy, with the interior holes closed by flood-filling the background
  from the corners. The silhouette is shipped; it keeps the ears, muzzle and open jaw readable at
  48 px.
- **Background layer** (`drawable/ic_launcher_background.xml`): a solid `#010202` rectangle; the disc
  gradient is gone because the foreground is opaque and the colour must match exactly.
- **In-app brand mark** (`drawable-*dpi/ic_panther_logo.png`, used at 28 dp by
  `PantherBrandHeader`): the same artwork in a rounded-square mask, replacing `ic_panther_logo.xml`.

The three former vector files (`ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`,
`ic_panther_logo.xml`) are deleted rather than kept beside the rasters: two files with one resource
name in different folders would be ambiguous to read, and nothing else referenced them.

## Consequences

- easier: one artwork is the whole visual identity — launcher, themed icon and header mark cannot
  drift apart again.
- easier: the safe-zone margin is a recorded number (`60/108`, ~24% clearance), so a later resize can
  be checked instead of guessed.
- harder: the icon is no longer vector, so the app carries ~430 KB of PNGs; the sizes are per-density
  and a new density needs a new render.
- harder: a replacement image needs the same feather-and-canvas treatment; pasting a trimmed
  rectangle will reintroduce the seam.
- unchanged: the launcher label, the `Panther` wordmark in headers, and the colour palette are not
  affected by this decision.
