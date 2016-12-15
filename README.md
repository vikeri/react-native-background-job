# react-native-background-job

Schedule background jobs that run your JavaScript when your app is in the background. 

The jobs will run even if the app has been closed and, by default, also persists over restarts.

## Getting started

`$ yarn add react-native-background-job`

or

`$ npm install react-native-background-job --save`

### Mostly automatic installation

`$ react-native link react-native-background-job`

### Manual installation

<!--
#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-background-job` and add `RNBackgroundJob.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBackgroundJob.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

-->

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.pilloxa.backgroundjob.RNBackgroundJobPackage;` to the imports at the top of the file
  - Add `new RNBackgroundJobPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-background-job'
  	project(':react-native-background-job').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-background-job/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-background-job')
  	```

## API


## Usage
```javascript
import BackgroundJob from 'react-native-background-job';

// TODO: What do with the module?
BackgroundJob;
```
  