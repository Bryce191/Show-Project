#include <iostream>
#include <vector>
#include "user.h"
#include "event.h"
#include "helpers.h"
#include "admin.h"
#include "organizer.h"
#include "attendee.h"
#include "marketing.h"
#include <limits>

using namespace std;

// Global containers
vector<User> users;
vector<Event> events;

void login();
void registerUser();
void mainMenu();

int main() {
    cout << "Starting Event Management System..." << endl;

    try {
        loadUsersFromFile(users);
        loadEventsFromFile(events);
        cout << "Data loaded successfully." << endl;
    }
    catch (const exception& e) {
        cerr << "Error loading data: " << e.what() << endl;
        cout << "Starting with minimal default data..." << endl;
    }

    displayIntro();

    try {
        mainMenu();
    }
    catch (const exception& e) {
        cerr << "Runtime error: " << e.what() << endl;
    }

    // Save data with error checking
    try {
        saveUsersToFile(users);
        saveEventsToFile(events);
        cout << "Data saved successfully." << endl;
    }
    catch (const exception& e) {
        cerr << "Error saving data: " << e.what() << endl;
    }

    cout << "\nThank you for using the Event Management System. Goodbye!\n";
    return 0;
}

void mainMenu() {
    int choice;
    do {
        clearScreen();
        cout << R"(+====================================================================+
|__/\\\\____________/\\\\__/\\\________/\\\__/\\\\\_____/\\\_        |
| _\/\\\\\\________/\\\\\\_\/\\\_______\/\\\_\/\\\\\\___\/\\\_       |
|  _\/\\\//\\\____/\\\//\\\_\/\\\_______\/\\\_\/\\\/\\\__\/\\\_      |
|   _\/\\\\///\\\/\\\/_\/\\\_\/\\\_______\/\\\_\/\\\//\\\_\/\\\_     |
|    _\/\\\__\///\\\/___\/\\\_\/\\\_______\/\\\_\/\\\\//\\\\/\\\_    |
|     _\/\\\____\///_____\/\\\_\/\\\_______\/\\\_\/\\\_\//\\\/\\\_   |
|      _\/\\\_____________\/\\\_\//\\\______/\\\__\/\\\__\//\\\\\\_  |
|       _\/\\\_____________\/\\\__\///\\\\\\\\\/___\/\\\___\//\\\\\_ |
|        _\///______________\///_____\/////////_____\///_____\/////__|
+====================================================================+
)";
        cout << "\n\n";
        cout << "Advertisement:\n";
        // Display top event advertisement
        displayTopEvent(events);
       
        cout << "+----------------------------------+\n";
        cout << "|            MAIN MENU             |\n";
        cout << "+----------------------------------+\n";
        cout << "|  [1] Login                       |\n";
        cout << "|  [2] Register                    |\n";
        cout << "|  [3] Exit                        |\n";
        cout << "+----------------------------------+\n";
        cout << "\nEnter your choice (1-3): ";

        while (!(cin >> choice) || choice < 1 || choice > 3) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input. Please enter a number between 1 and 3: ";
        }

        switch (choice) {
        case 1: 
            login(); 
            break;
        case 2: 
            registerUser(); 
            break;
        case 3: 
            return;
        }
    } while (choice != 3);
}

void login() {
    clearScreen();
    cout << "===== LOGIN =====\n\n";

    string username, password;
    cout << "Username: ";
    cin >> username;
    password = getPasswordInput();

    bool found = false;
    for (User& user : users) {
        if (user.username == username && user.password == password) {
            found = true;
            cout << "\nLogin successful! Welcome, " << user.name << "!\n";
            pauseScreen();
            clearScreen();

            if (user.role == "admin") {
                adminMenu(user);
            }
            else if (user.role == "organizer") {
                organizerMenu(user);
            }
            else if (user.role == "attendee") {
                attendeeMenu(user);
            }
            break;
        }
    }


    if (!found) {
        cout << "\nInvalid username or password. Please try again.\n";
        pauseScreen();
        clearScreen();
    }
}



void registerUser() {
    clearScreen();
    cout << "===== REGISTER =====\n\n";
    cout << "Enter 0 at any time to cancel registration\n\n";

    User newUser;
    newUser.id = generateUserId();

    cout << "Enter username: ";
    cin >> newUser.username;

    if (newUser.username == "0") {
        cout << "Registration cancelled.\n";
        pauseScreen();
        clearScreen();
        return;
    }

    for (const User& user : users) {
        if (user.username == newUser.username) {
            cout << "Username already exists. Please choose another.\n";
            pauseScreen();
            clearScreen();
            return;
        }
    }

    newUser.password = getPasswordInput("Enter password: ");
    if (newUser.password == "0") {
        cout << "Registration cancelled.\n";
        pauseScreen();
        clearScreen();
        return;
    }

    string nameInput;
    cin.ignore(numeric_limits<streamsize>::max(), '\n');
    do {
        cout << "Enter your name: ";
        
        getline(cin, nameInput);

        if (nameInput == "0") {
            cout << "Registration cancelled.\n";
            pauseScreen();
            clearScreen();
            return;
        }

        if (nameInput.empty()) {
            cout << "Name cannot be empty. Please enter your name or '0' to cancel.\n";
        }

    } while (nameInput.empty());  

    newUser.name = nameInput;

    string emailInput;
    do {
        cout << "Enter email: ";
        cin >> emailInput;

        if (emailInput == "0") {
            cout << "Registration cancelled.\n";
            pauseScreen();
            clearScreen();
            return;
        }

        if (!isValidEmail(emailInput)) {
            cout << "Invalid email format. Please try again.\n";
        }
        else {
            newUser.email = emailInput;
            break;
        }
    } while (true);

    int roleChoice;
    cout << "\nSelect your role:\n";
    cout << "1. Event Organizer\n";
    cout << "2. Attendee\n";
    cout << "Enter choice (1-2, 0 to cancel): ";

    roleChoice = getIntInput(0, 2, true); 

    if (roleChoice == 0) {
        cout << "Registration cancelled.\n";
        pauseScreen();
        clearScreen();
        return;
    }

    newUser.role = (roleChoice == 1) ? "organizer" : "attendee";

    users.push_back(newUser);
    saveUsersToFile(users);

    cout << "\nRegistration successful! You can now login with your credentials.\n";
    pauseScreen();
    clearScreen();
}