<body>
  <div align="center">
  <div>
    <img height="200" src="https://drygo-assets.vercel.app/uploads/repository/xmusic/icon.webp"  />
  </div>
  
  ###
  
  <h1>xMusic 🎵</h1>
  
  ###
  
  <p>A plugin that manage a background music system.</p>
  
  </div>

  <h2 align="center">🔧 • Commands</h2>
  <section>
  <h3>
    Plugin Commands
  </h3>
  <ul>
    <li>
      <strong>/xmusic play <song|playlist> <key> [player/*] -</strong> Play a song or playlist to player(s).
    </li>
    <li>
      <strong>/xmusic stop <song|playlist> <player/*> [finish] -</strong> Stop playing music for player(s).
    </li>
    <li>
      <strong>/xmusic skip <player/*> -</strong> Skip the current song for player(s).
    </li>
    <li>
      <strong>/xmusic reload -</strong> Reload the plugin configuration.
    </li>
    <li>
      <strong>/xmusic help -</strong> Displays the command list.
    </li>
    <li>
      <strong>/xmusic info -</strong> Displays plugin information.
    </li>
  </ul>
  </section>
  <section>
  <h3>
    Play Command Details
  </h3>
  <ul>
    <li>
      <strong>/xmusic play song <key> [player/*] -</strong> Play a specific song.
    </li>
    <li>
      <strong>/xmusic play playlist <key> [player|*] -</strong> Play a playlist.
    </li>
  </ul>
  </section>
  <section>
  <h3>
    Manage Music Playback
  </h3>
  <ul>
    <li>
      <strong>/xmusic stop song <player/*> [finish] -</strong> Stop a song; 'finish' plays the song till the end.
    </li>
    <li>
      <strong>/xmusic stop playlist <player/*> [finish] -</strong> Stop a playlist; 'finish' finishes current song before stopping.
    </li>
    <li>
      <strong>/xmusic skip <player/*> -</strong> Skip the current song for the player(s).
  </ul>
  </section>
  
  ###
  <h2 align="center">⚙️ • Using API</h2>
</body>

### Add Maven dependency
```xml
<dependencies>
  <dependency>
    <groupId>com.github.xDrygo</groupId>
    <artifactId>xMusic</artifactId>
    <version>0.0.13</version>
    <scope>provided</scope>
  </dependency>
</dependencies>

<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```
### Add Gradle dependency (settings.gradle)
```gradle
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }
  }
}

dependencies {
  implementation 'com.github.xDrygo:xMusic:0.0.13'
}
```
### Add Gradle dependency (settings.gradle.kts)
```gradle.kts
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
  }
}

dependencies {
  implementation("com.github.xDrygo:xMusic:0.0.13")
}
```

---
