mvn archetype:generate -DgroupId=data.scripts -DartifactId=better-colony-mod -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

mvn install:install-file -Dfile="C:\Program Files (x86)\Fractal Softworks\Starsector\starsector-core\starfarer.api.jar" -DgroupId=com.fs.starfarer.api -DartifactId=starsector_api -Dversion=0.9.1 -Dpackaging=jar -DgeneratePom=true
