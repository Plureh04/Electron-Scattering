# Electron-Scattering analysis

In order to run this code, you will need nfs data installed onto your system

I recommend running this on an IDE, but the terminal should work fine.

In an IDE: 

You will need to upload coatjava libraries into the IDE by: 
  1. Heading to the four lines at the top left
  2. Click on project structure
  3. Click on libraries and click the + sign to add a "java" project library
  4. Head on to your coatjava folder and add the coatjava library
  5. Now click the + sign where you will add the files and head over to the coatjava folder
  6. Click on the lib folder (you will see three folders)
  7. Click the clas folder and add the "coat-libs-...".jar file and click apply
  8. Repeat the same process for the services folder ("clas12detector-ec-1.0....".jar) and utils ("jclara-4.3-...jar")
I also recommend (not mandatory) implmenting 2d graphics from freehep:
  1. After you go to project structure and libaries, you will add a "maven" project library
  2. From the search bar, type "freehep" and you will get options to select
  3. Select the newest version (2.4) for "graphics2d", "graphicsbase", & "freehep-io-2.2.2.jar"
  4. Apply all to the project and it should be implemented
1. Download or copy the Carbon.java and PB.java from this repo
     Implement a class in your IDE with the same class name (or change it)
3. To see proton identification, run PID.java
4. To look at the kinematics, run kinematics.java
5. To look at the ratios of the proton and electorn, run the files provided in the Ratios file
6. To look at the multiplicity ratios, run the provided files inside the mult_ratios file
