# Attribution

This document lists all third-party libraries, icons, services, and acknowledgments used in Kate.

---

## Libraries

### AndroidX
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://developer.android.com/jetpack/androidx
- Includes Core, Activity, Lifecycle, Preference, DocumentFile, ExifInterface, Navigation, Window, and WorkManager.

### Jetpack Compose
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://developer.android.com/jetpack/compose
- Powers the whole interface, including Material 3 and the extended Material icons.

### Jetpack Glance
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://developer.android.com/jetpack/compose/glance
- Powers the home screen widgets (`androidx.glance:glance-appwidget`).

### AndroidX Media3
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://developer.android.com/guide/topics/media/media3
- Powers playback, the media session, notification and lock screen controls, and Android Auto (`media3-exoplayer`, `media3-session`).

### Sonic
- **Author:** Bill Cox
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/waywardgeek/sonic
- Changes playback speed while keeping the original pitch, through its port in Media3.

### Material Components for Android
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/material-components/material-components-android

### Oboe
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/google/oboe
- Powers Kate's native audio engine for high-resolution and low-latency output (`com.google.oboe:oboe`).

### TagLib
- **Author:** Scott Wheeler and the TagLib contributors
- **License:** [GNU Lesser General Public License 2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html) or [Mozilla Public License 1.1](https://www.mozilla.org/MPL/1.1/)
- **URL:** https://taglib.org
- Reads and writes the tags, cover art, and lyrics in your music files.

### TagLib for Android
- **Author:** Kyant
- **License:** [See LICENSE](https://github.com/Kyant0/taglib/blob/main/LICENSE)
- **URL:** https://github.com/Kyant0/taglib
- Brings TagLib to Android (`io.github.kyant0:taglib`).

### Coil
- **Author:** Coil Contributors
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/coil-kt/coil
- Loads cover art and artist images (`io.coil-kt.coil3:coil-compose`).

### MaterialKolor
- **Author:** Jordon de Hoog
- **License:** [MIT License](https://opensource.org/licenses/MIT)
- **URL:** https://github.com/jordond/materialkolor
- Picks colors from album art to theme the app and player (`com.materialkolor:material-color-utilities`).

```
MIT License

Copyright (c) 2023 Jordon de Hoog

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Material Color Utilities
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/material-foundation/material-color-utilities
- The color science behind MaterialKolor.

### Kotlin Coroutines
- **Author:** JetBrains
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/Kotlin/kotlinx.coroutines

### Markwon
- **Author:** Noties
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/noties/Markwon
- Shows the changelog, attribution, and supporters list inside the app.

### OkHttp
- **Author:** Square
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/square/okhttp
- Handles every network request: online lyrics, tag lookup, cover art, artist images, update checks and downloads, and the in-app changelog, attribution, and supporters list.

---

## Online Services

Kate only connects to these services when you ask it to. Each service has its own terms of use.

### Lyrics

- **[LRCLIB](https://lrclib.net)** — synced and plain lyrics
- **[NetEase Cloud Music](https://music.163.com)** — synced lyrics
- **[Kugou](https://www.kugou.com)** — word-by-word synced lyrics
- **[lrc.red](https://lrc.red)** — word-by-word synced lyrics
- **[LyricsPlus](https://github.com/ibratabian17/YouLyPlus)** — word-by-word synced lyrics
- **[AMLL TTML DB](https://github.com/amll-dev/amll-ttml-db)** — word-by-word synced lyrics from the Apple Music-like Lyrics community

### Tags, Cover Art & Artist Images

- **[MusicBrainz](https://musicbrainz.org)** — song and album info
- **[Cover Art Archive](https://coverartarchive.org)** — album covers
- **[iTunes Search API](https://performance-partners.apple.com/search-api)** — song info and album covers
- **[Deezer API](https://developers.deezer.com)** — song info, album covers, and artist images

Special thanks to everyone who builds and maintains these services and the communities who contribute to them.

---

## Icons & Assets

### Material Icons
- **Author:** Google
- **License:** [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- **URL:** https://github.com/google/material-design-icons

---

## Acknowledgments

### Theme — From Clint
The theming system in Kate comes from [Clint Browser](https://github.com/jhaiian/ClintBrowser) and [Clint Gecko](https://github.com/jhaiian/ClintGecko). Kate shares the same accent colors, surface intensities, and theme design as the rest of the Jhaiian ecosystem.

### UI — Inspired by Poweramp
Some parts of Kate's interface drew inspiration from [Poweramp](https://powerampapp.com) by Max MP. Special credit to the Poweramp team for their excellent music player design, which influenced parts of Kate's look and feel.
