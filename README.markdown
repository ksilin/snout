## parallel data fetching with akka

Follow these steps to get started:

1. Launch SBT:

        $ sbt

2. Compile everything and run all tests:

        > test

3. Start the application:

        > re-start

4. Send a request to one of the endpoints: http://localhost:9090/[ser|par|super|fwd]

5. Stop the application:

        > re-stop
        
### build & deploy

native packager to the rescue: https://github.com/sbt/sbt-native-packager

`sbt universal:packageBin` will create a zip for you in side the target folder. Unzip it and run the binary inside the `bin` folder.  
        
