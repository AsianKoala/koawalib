## koawalib
FTC library written by Neil Mehra, member of team 14607 and alumentor of team 15167. 
The goal of this library is to enable teams of all levels to perform at a sufficient degree without 
programming knowledge/experience being a limiting factor. Shoutout to [wpilib](https://github.com/wpilibsuite/allwpilib)
and [technolib](https://github.com/technototes/TechnoLib) for inspiration.

## Installation
- Add the following lines in the repositories block of  ```build.dependencies.gradle```:
```
repositories {
...
  maven { url = 'https://maven.brott.dev/' }
  maven { url = 'https://jitpack.io' }
}
```
- Then in your ```/TeamCode/build.gradle``` add this line to your ```dependencies``` block, where "0.0.4" is the newest release version number
```
dependencies {
  ...
  implementation 'com.github.AsianKoala:koawalib:0.0.4'
}
```
- If you wish to have the most up to date version, instead add this line
```
implementation 'com.github.AsianKoala:koawalib:-SNAPSHOT'
```

## Example usage
koawalib-quickstart is coming (eventually)

## Documentation
documentation is also coming (eventually)
