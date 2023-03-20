# Key App (Android)

![Frame 1523524](https://user-images.githubusercontent.com/19359742/219026384-e72e5f10-6905-4459-b6ff-376e614dad0c.png)

## Getting Started
- Download the latest Android Studio.
- Clone this repository.
- In Android Studio, create an Android Virtual Device (AVD) that the emulator can use to install and run your app.
- In the toolbar, select your app from the run/debug configurations drop-down menu.
- From the target device drop-down menu, select the AVD that you want to run your app on.
- Click Run.

### API Keys requirements
For app to run properly you need to create and fill some 3rd party APIs keys with your own values. 
1. Create `apikey.properties` file at the project root directory. `*.properties` file type is a file with key-value text that Gradle parses on project build
2. The contents of `apikey.properties` should be the following:
```groovy
intercomApiKey="your_key"
intercomAppId="your_key"
rpcPoolApiKey="your_key"
moonpayKey="your_key"
comparePublicKey="your_key"
amplitudeKey="your_key"
```
Where:
`intercomApiKey`, `intercomAppId` - API keys for [Intercom](https://www.intercom.com) library
`rpcPoolApiKey` - API key for p2p.rpcpool.com
`moonpayKey` - API key for api.moonpay.com
`comparePublicKey` - API key cryptocompare.com
`amplitudeKey` - API key for [Amplitude](https://developers.amplitude.com/docs/android) library

3. Create `torus.properties` file at the root of the project. It should include the information about the verifiers and subverifiers
`TORUS_VERIFIER_DEBUG` - The verifier for testing debug builds
`TORUS_VERIFIER_FEATURE` - The verifier for testing feature builds, i.e. firebase feature builds
`TORUS_VERIFIER_RELEASE` - The verifier for testing release builds, i.e. for playstore builds

There are some subverifiers are used for release builds:
`TORUS_SUB_VERIFIER_RELEASE_LOCAL`
`TORUS_SUB_VERIFIER_RELEASE_PRODUCTION`
`TORUS_SUB_VERIFIER_RELEASE_FIREBASE`

## Main stack

### Libraries
- **Koin** - A pragmatic lightweight dependency injection (DI) framework for Kotlin developers. Written in pure Kotlin using functional resolution only: no proxy, no code generation, no reflection.
- **Coroutines** - A coroutine is a concurrency design pattern that you can use on Android to simplify code that executes asynchronously. Coroutines were added to Kotlin in version 1.3 and are based on established concepts from other languages.
- **Retrofit** - Retrofit is a REST Client for Java and Android. It makes it relatively easy to retrieve and upload JSON (or other structured data) via a REST based webservice.
- **GSON** - Gson is a Java library that can be used to convert classes into their JSON representation. It can also be used to convert a JSON string to an equivalent Kotlin object.
- **Room** - The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.
- **Glide** - Glide is a fast and efficient open source media management and image loading framework for Android that wraps media decoding, memory and disk caching, and resource pooling into a simple and easy to use interface.

### Architecture
- **MVP** - The MVP (Model-View-Presenter) pattern helps to completely separate the business and presentation logic from the UI, and the business logic and UI can be clearly separated for easier testing and easier maintenance.
- **Clean Architecture** - Clean Architecture combines a group of practices that produce systems with the following characteristics: Testable; UI-independent (the UI can easily be changed without changing the system); Independent of databases, frameworks, external agencies, and libraries.

## Contributing
The best way to submit feedback and report bugs is to open a GitHub issue. Please be sure to include:
- your operating system
- device
- version number
- steps to reproduce reported bugs

### Code Style
How to apply:
1. `Settings / Preferences -> Editor -> Code Style`
2. In opened window choose `Project` schema.
