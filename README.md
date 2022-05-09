## koawalib
koawalib is a general purpose FTC library written in Kotlin, inspired by [@wpilib](https://github.com/wpilibsuite/allwpilib)

## Installation
- Add the following lines before the ```dependencies``` block in ```/TeamCode/build.gradle```
```
repositories {
  maven { url = 'https://maven.brott.dev/' }
  maven { url = 'https://jitpack.io' }
}
```
- Then add this line to your ```dependencies``` block, where "1.0.0" is a release tag
```
implementation 'com.github.AsianKoala:koawalib:1.0.0'
```

- ### Snapshot
  - If you wish to have the most recent commit of the repository rather than a release, consider using a snapshot.
  - To use the snapshot version, first add this code block. This changes the default gradle caching time settings.
    ```
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
        resolutionStrategy.cacheDynamicVersionsFor 10, 'minutes'
    }
    ```
  - Then add this line to the ```dependencies``` block, instead of the one used above.
    ```
    implementation 'com.github.AsianKoala:koawalib:master-SNAPSHOT'
    ```
  - If you want to use a specific commit that isn't the newest or in a release, use this syntax for the dependency, where COMMIT-HASH is the hash of the commit you want to use.
    ```
    implementation 'com.github.AsianKoala:koawalib:COMMIT-HASH"
    ```
  - If for some reason you want to use the dev branch, which has the newest features but no guarantee on stability, add this line
    ```
    implementation 'com.github.AsianKoala:koawalib:dev-SNAPSHOT"
    ```


## Usage
[FTC team 2Σ's offseason repo](https://github.com/two-sigma/offseason)  
[koawalib docs](https://asiankoala.github.io/koawalib/)  
[koawalib-quickstart](https://github.com/AsianKoala/koawalib_quickstart)  
