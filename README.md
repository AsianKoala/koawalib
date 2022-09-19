# koawalib
koawalib is a general purpose FTC library written in Kotlin, inspired by [@wpilib](https://github.com/wpilibsuite/allwpilib)

## Installation
#### Easy Way (creating a new repo)
Go to [the template repo](https://github.com/AsianKoala/koawalib-template) and fork it.

#### Hard Way (adding koawalib to your own repo)
- Add the following lines before the ```dependencies``` block in ```/TeamCode/build.gradle```
```
repositories {
  maven { url = 'https://maven.brott.dev/' }
  maven { url = 'https://www.jitpack.io' }
}
```
- Then add this line to your ```dependencies``` block, where "1.0.0" is a release tag
```
implementation 'com.github.AsianKoala:koawalib:1.0.0'
```

- #### Snapshot
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
  - If for some reason you want to use the dev branch, add this line
    ```
    implementation 'com.github.AsianKoala:koawalib:dev-SNAPSHOT"
    ```


## Usage
[my personal code repo](https://github.com/ftc-noteam/PP-Public)  
[koawalib docs](https://asiankoala.github.io/koawalib/)  
