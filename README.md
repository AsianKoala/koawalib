# koawalib
koawalib is a general purpose FTC library written in Kotlin

## Installation
#### Easy Way (creating a new repo)
Go to [the template repo](https://github.com/AsianKoala/koawalib-template) and fork/clone it.

#### Hard Way (adding koawalib to your existing repo)
- Add the following lines before the ```dependencies``` block in ```/TeamCode/build.gradle```
```
repositories {
  maven { url = 'https://www.jitpack.io' }
}
```
- Then add this line to your ```dependencies``` block, where "1.1.0" is a release tag
```
implementation 'com.github.AsianKoala:koawalib:1.3.7'
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

## Example Usage
[my personal code repo](https://github.com/ftc-noteam/PP-Public)  
[koawalib docs](https://asiankoala.github.io/koawalib/)  
