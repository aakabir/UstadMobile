# Content Scraper

A content scraper to find and download content to make them available for offline use

###Find and Scrap Edraak K12 Content

####To Find All Edraak Content

All the content for edraak K12 is available in the url given below in the gradle task.

>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41" -PfindEdraakDir="C:\edraak\"

Edraak have 6 different categories for courses:

Numbers and processes: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a608815f3a50d049abf68e9/?states_program_id=41" -PfindEdraakDir="C:\edraak\"

Alegbra and Patterns: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a6088188c9a02049a3e69e5/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Engineering and Measurement: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a608819f3a50d049abf68ea/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Spaces and Sizes: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a608828f3a50d049b1d2cc6/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Statistics and Probability: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a60881e6b9064043689772d/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Triangles:
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a60881bf3a50d049b1d2cc5/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


#### For Specific Edraak Course Content

To Download a specific exercise or quiz in one of the 6 categories:
Need to use the inspector in the browser for the specific exercise. 

In the Networks Tab, filter by XHR and refresh the page. 2 edraak url will load:- 

1. url for the specific category you are in 
2. url for the course/quiz being loaded 

Take the 2nd link address and enter the gradle task below to download and zip the course.

>$ gradlew scrapeContent -PedraakUrl="https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41" -PedraakDir="C:\edraak\"


### Find and Scrap Phet Simulation

#### Find All Phet Simulations

All supported html simulations can be found at https://phet.colorado.edu/en/simulations/category/html

It will find and download all the simulations and its translations and stores them into a zip file.

>$ gradlew scrapeContent -PfindPhetUrl="https://phet.colorado.edu/en/simulations/category/html" -PfindPhetDir="C:\phet\"

You can also download all the simulations in a specific category:- 
Example link of specific category: https://phet.colorado.edu/en/simulations/category/physics/work-energy-and-power

>$ gradlew scrapeContent -PfindPhetUrl="https://phet.colorado.edu/en/simulations/category/physics/work-energy-and-power" -PfindPhetDir="C:\phet\"



#### Download Specific Phet Simulation

Example: https://phet.colorado.edu/en/simulation/acid-base-solutions

Downloads the simulation and all its translations

>$ gradlew scrapeContent -PphetUrl="https://phet.colorado.edu/en/simulation/acid-base-solutions" -PpetDir="C:\phet\"