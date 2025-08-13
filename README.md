# 🌟 Electron-Scattering analysis

# ℹ️ Overview

1. Installations
   - In order to run this code, you will need nfs data installed onto your system. 
   - An IDE is recommended (intellij, VisualStudios, eclipse)
   - Coatjava library will be needed (can extract from the JeffersonLab github)

# IDE 

1. Upload coatjava libraries into the IDE by: 

   - Heading to the four lines at the top left
   - Click on project structure
   - Click on libraries and click the + sign to add a "java" project library
   - Head on to your coatjava folder and add the coatjava library
   - Now click the + sign where you will add the files and head over to the coatjava folder
   - Click on the lib folder (you will see three folders)
   - Click the clas folder and add the "coat-libs-...".jar file and click apply
   - Repeat the same process for the services folder ("clas12detector-ec-1.0....".jar) and utils ("jclara-4.3-...jar")

# Optional graphics

2. Implmenting 2d graphics from freehep:
   - After you go to project structure and libaries, you will add a "maven" project library
   - From the search bar, type "freehep" and you will get options to select
   - Select the newest version (2.4) for "graphics2d", "graphicsbase", & "freehep-io-2.2.2.jar"
   - Apply all to the project and it should be implemented properly

# Terminal 

1. Fork or Clone this repo
   - Make sure coatjava is installed into your system
   - To fork this repo, head to the front page and click fork
   - To clone this repo do a "git clone [url]"
   - 
# Files

Which files to Run:

1. Download or copy the Carbon.java and PB.java from this repo
2. To see proton identification, run PID.java
3. To look at the kinematics, run kinematics.java
4. To look at the ratios of the proton and electorn, run the files provided in the Ratios file
5. To look at the multiplicity ratios, run the provided files inside the mult_ratios file
