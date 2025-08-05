# projectTimer

I want to make a kotlin android app to time and save the amount of work I do on different projects. The app should be simple and have the following features :
- when the app start a welcome page appears for 2sec with the app title and version
- 3 tabs, accessible with a bottom bar
- first tab is a list of projects (list icon on the tab button), with a plus button on the bottom right to add new projects. each project in the list is a button, when press it start a timer (meaning I'm working on this project). Each item should have the number of hours done number of hours expected displayed. Each item should also have a three dots button on the extreme right to edit the project opening a project edit page (name, color, expected number of hour). Each item should also be grabable when press hold for 2sec, to slide them up or down in the list and change the order in the list. When a project is clicked again it ask if you want to pause or terminate the session if so it stop the timer and prompt for a title or note or canceling of the session before saving it in a json file in the phone.
- the second tab is a chronological list of the recorded working sessions(calendar icon on the tab button). Each session displayed a  rounded square box with the project color and the title of the session. There is a day delimitation with the date number in the middle of an horizontal line. The list is scrollable. If a entry is clicked it open an editing page with the project, title, note, date, duration and a save and  delete button. There is also a plus button on the bottom right to add manually an entry with the entry editing page.
- the third tab is a setting tab (gear icon). General parameters. Export button to export the working session in a ICS format: prompting for the range start and end date of the sessions to export.
- I want each screen to be defined in different page and each page should have it's mechanism/model in another page.

i want the navigation to be handle in a separate file like:
fun AppNavigation() {
val navController = rememberNavController()
NavHost(navController, startDestination = "welcome") {
composable("welcome")        { WelcomeScreen(navController) }
composable("login")          { LoginScreen(navController)   }
composable("home")           { HomeScreen(navController)    }
composable("diary")          { DiaryListScreen(navController)   }
composable("graph")          { GraphScreen(navController)   }
composable("about")          { AboutScreen(navController)   }
composable("diary_list") { DiaryListScreen(navController) }
composable("diary_edit/{entryId}") { backStack ->
    val id = backStack.arguments?.getString("entryId") ?: "new"
    DiaryEditScreen(navController, entryId = id)
    }

// now only one questionnaire route
composable("questionnaire") {
    QuestionnaireListScreen(
        onBackToHome = { navController.popBackStack("home", inclusive = false) }
    )
}

composable("settings") { SettingsScreen(navController) }
composable("about") { AboutScreen(navController) }
composable("logout") { LogoutScreen(navController) }
    }

Can you process, clean up and add any missing features to this app description to prepare for the code creation of the app 