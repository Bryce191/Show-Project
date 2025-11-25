#include "admin.h"
#include <iomanip>
#include <algorithm>
#include "helpers.h"
#include "user.h"
#include "event.h"

using namespace std;

extern vector<User> users;
extern vector<Event> events;

void adminMenu(User& admin) {
    int choice;
    do {
        clearScreen();
        string adminName = admin.name;
        cout << "\n";
        cout << "+=================================================+\n";
        cout << "|                 ADMIN MENU                      |\n";
        cout << "+=================================================+\n";
        cout << " Welcome, " << adminName << " (Admin)                         \n\n";
        cout << "+-------------------------------------------------+\n";
        cout << "|  1. View All Users                              |\n";
        cout << "|  2. Add User                                    |\n";
        cout << "|  3. Delete User                                 |\n";
        cout << "|  4. View All Events                             |\n";
        cout << "|  5. Remove Event                                |\n";
        cout << "|  6. System Statistics                           |\n";
        cout << "|  7. Event Status                                |\n";
        cout << "|  8. View Ratings & Complaints                   |\n";
        cout << "|  9. LogOut                                      |\n";
        cout << "+=================================================+\n";
        

        cout << "Enter your choice (1-9): ";
        while (!(cin >> choice) || choice < 1 || choice > 9) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input. Please enter a number between 1 and 9: ";
        }

        switch (choice) {
        case 1: {
            clearScreen();
            cout << "===== ALL USERS =====\n\n";
            cout << setw(5) << "ID" << setw(15) << "Username" << setw(20) << "Name"
                << setw(25) << "Email" << setw(12) << "Role" << endl;
            cout << string(80, '-') << endl;

            for (const User& user : users) {
                cout << setw(5) << user.id << setw(15) << user.username << setw(20) << user.name
                    << setw(25) << user.email << setw(12) << user.role << endl;
            }
            cout << "\nPress Enter to continue...";
            cin.ignore();
            cin.get();
            clearScreen();
            break;
        }
        case 2: {
            clearScreen();
            cout << "===== ADD USER =====\n\n";
            cout << "Enter 0 at any time to cancel user creation\n\n";

            User newUser;
            newUser.id = generateUserId();

            cout << "Enter username: ";
            cin >> newUser.username;

            if (newUser.username == "0") {
                cout << "User creation cancelled.\n";
                pauseScreen();
                clearScreen();
                break;
            }

            // Check if username exists
            bool usernameExists = false;
            for (const User& user : users) {
                if (user.username == newUser.username) {
                    usernameExists = true;
                    break;
                }
            }

            if (usernameExists) {
                cout << "Username already exists.\n";
                pauseScreen();
                clearScreen();
                break;
            }

            newUser.password = getPasswordInput("Enter password: ");
            if (newUser.password == "0") {
                cout << "User creation cancelled.\n";
                pauseScreen();
                clearScreen();
                break;
            }

            string nameInput;
            cin.ignore(numeric_limits<streamsize>::max(), '\n'); // Clear the input buffer
            do {
                cout << "Enter your name: ";
                getline(cin, nameInput);

                if (nameInput == "0") {
                    cout << "Registration cancelled.\n";
                    pauseScreen();
                    clearScreen();
                    break;
                }

                if (nameInput.empty()) {
                    cout << "Name cannot be empty. Please enter your name or '0' to cancel.\n";
                }

            } while (nameInput.empty());

            if (nameInput == "0") break; // Check if user cancelled during name input

            newUser.name = nameInput;

            string emailInput;
            do {
                cout << "Enter email: ";
                cin >> emailInput;

                if (emailInput == "0") {
                    cout << "User creation cancelled.\n";
                    pauseScreen();
                    clearScreen();
                    break;
                }

                if (!isValidEmail(emailInput)) {
                    cout << "Invalid email format. Please try again.\n";
                }
                else {
                    newUser.email = emailInput;
                    break;
                }
            } while (true);

            if (emailInput == "0") break; // Check if user cancelled during email input

            // Role selection
            int roleChoice;
            cout << "\nSelect user role:\n";
            cout << "1. Admin\n";
            cout << "2. Organizer\n";
            cout << "3. Attendee\n";
            cout << "Enter choice (1-3, 0 to cancel): ";

            roleChoice = getIntInput(0, 3, true);

            if (roleChoice == 0) {
                cout << "User creation cancelled.\n";
                pauseScreen();
                clearScreen();
                break;
            }

            switch (roleChoice) {
            case 1: newUser.role = "admin"; break;
            case 2: newUser.role = "organizer"; break;
            case 3: newUser.role = "attendee"; break;
            }

            users.push_back(newUser);
            saveUsersToFile(users);

            cout << "\nUser added successfully!\n";
            pauseScreen();
            clearScreen();
            break;
        }
  
        case 3: {
            clearScreen();
            cout << "===== DELETE USER =====\n\n";

            cout << "===== ALL USERS =====\n\n";
            cout << setw(5) << "ID" << setw(15) << "Username" << setw(20) << "Name"
                << setw(25) << "Email" << setw(12) << "Role" << endl;
            cout << string(80, '-') << endl;

            for (const User& user : users) {
                cout << setw(5) << user.id << setw(15) << user.username << setw(20) << user.name
                    << setw(25) << user.email << setw(12) << user.role << endl;
            }

            cout << "Enter user ID to delete (0 to cancel): "; 
            int userId = getIntInput(0, INT_MAX, true); 

            if (userId == 0) {
                cout << "Operation cancelled.\n";
                pauseScreen();
                break;
            }
           

            bool found = false;
            for (auto it = users.begin(); it != users.end(); ++it) {
                if (it->id == userId) {
                    found = true;
                    cout << "Are you sure you want to delete user " << it->username << "? (y/n): ";
                    char confirm;
                    cin >> confirm;

                    if (tolower(confirm) == 'y') {
                        for (auto& event : events) {
                            if (event.organizerId == userId) {
                                event.organizerId = -1;
                            }

                            auto attendeeIt = find(event.attendees.begin(), event.attendees.end(), userId);
                            if (attendeeIt != event.attendees.end()) {
                                event.attendees.erase(attendeeIt);
                            }
                        }

                        users.erase(it);
                        saveUsersToFile(users);
                        saveEventsToFile(events);
                        cout << "User deleted successfully.\n";
                    }
                    else {
                        cout << "Deletion canceled.\n";
                    }
                    break;
                }
            }

            if (!found) {
                cout << "User with ID " << userId << " not found.\n";
            }
            cout << "\nPress Enter to continue...";
            cin.ignore();
            cin.get();
            clearScreen();
            break;
        }
        case 4: {
            clearScreen();
            cout << "===== ALL EVENTS =====\n\n";

            if (events.empty()) {
                cout << "No events available at the moment.\n";
                pauseScreen();
                break;
            }

            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(10) << "Time" << setw(20) << "Location" << setw(12) << "Status"
                << setw(15) << "Organizer" << setw(10) << "Attendees" << endl;
            cout << string(130, '-') << endl;

            for (const Event& event : events) {
                string organizerName = "Unknown";
                for (const User& user : users) {
                    if (user.id == event.organizerId) {
                        organizerName = user.name;
                        break;
                    }
                }

                cout << setw(5) << event.id
                    << setw(25) << (event.title.length() > 20 ? event.title.substr(0, 20) + "..." : event.title)
                    << setw(12) << event.date
                    << setw(10) << event.time
                    << setw(20) << (event.location.length() > 15 ? event.location.substr(0, 15) + "..." : event.location)
                    << setw(12) << statusToString(event.status)
                    << setw(15) << (organizerName.length() > 12 ? organizerName.substr(0, 12) + "..." : organizerName) << setw(10) << event.attendees.size() << endl;
            }

            cout << "\nEnter an event ID to view details (or 0 to go back): ";
            int eventId;
            while (!(cin >> eventId)) {
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
                cout << "Invalid input. Please enter a numeric ID: ";
            }

            if (eventId != 0) {
                bool found = false;
                for (const Event& event : events) {
                    if (event.id == eventId) {
                        found = true;
                        clearScreen();
                        cout << "===== EVENT DETAILS =====\n\n";
                        cout << "Title: " << event.title << endl;
                        cout << "Description: " << event.description << endl;
                        cout << "Date: " << event.date << endl;
                        cout << "Time: " << event.time << endl;
                        cout << "Location: " << event.location << endl;

                        string organizerName = "Unknown";
                        for (const User& user : users) {
                            if (user.id == event.organizerId) {
                                organizerName = user.name;
                                break;
                            }
                        }
                        cout << "Organizer: " << organizerName << endl;
                        cout << "Expected Participants: " << event.expectedParticipants << endl;
                        cout << "Current Attendees: " << event.attendees.size() << endl;

                        // theme details
                        if (event.themeName != "None") {
                            cout << "Theme: " << event.themeName << endl;
                            cout << "Vendor: " << event.vendorName << endl;
                            cout << "Theme Cost: RM" << fixed << setprecision(2) << event.themeCost << endl;
                        }

                        // advertisement
                        if (!event.marketing.empty()) {
                            cout << "\n*** Advertisement ***\n";
                            cout << event.marketing << endl;
                            cout << "*********************\n";
                        }

                        // attendees list
                        if (!event.attendees.empty()) {
                            cout << "\nAttendees List:\n";
                            cout << setw(5) << "ID" << setw(20) << "Name" << setw(25) << "Email" << endl;
                            cout << string(60, '-') << endl;
                            for (int userId : event.attendees) {
                                for (const User& user : users) {
                                    if (user.id == userId) {
                                        cout << setw(5) << user.id
                                            << setw(20) << user.name
                                            << setw(25) << user.email << endl;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }

                if (!found) {
                    cout << "Event not found.\n";
                }
                pauseScreen();
            }
            break;
        }
        case 5: { 

            clearScreen();
            cout << "===== REMOVE EVENT =====\n\n";

            if (events.empty()) {
                cout << "No events available at the moment.\n";
                pauseScreen();
                break;
            }

            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(10) << "Time" << setw(20) << "Location" << setw(12) << "Status"
                << setw(15) << "Organizer" << setw(10) << "Attendees" << endl;
            cout << string(95, '-') << endl;

            for (const Event& event : events) {
                string organizerName = "Unknown";
                for (const User& user : users) {
                    if (user.id == event.organizerId) {
                        organizerName = user.name;
                        break;
                    }
                }

                cout << setw(5) << event.id
                    << setw(25) << (event.title.length() > 20 ? event.title.substr(0, 20) + "..." : event.title)
                    << setw(12) << event.date
                    << setw(10) << event.time
                    << setw(20) << (event.location.length() > 15 ? event.location.substr(0, 15) + "..." : event.location)
                    << setw(12) << statusToString(event.status)
                    << setw(15) << (organizerName.length() > 12 ? organizerName.substr(0, 12) + "..." : organizerName) << endl;
            }

            
            cout << "\nEnter event ID to remove (0 to cancel): "; 
            int eventId = getIntInput(0, INT_MAX, true); 

            if (eventId == 0) {
                cout << "Operation cancelled.\n";
                pauseScreen();
                break;
            }

            bool found = false;
            for (auto it = events.begin(); it != events.end(); ++it) {
                if (it->id == eventId) {
                    found = true;
                    cout << "Are you sure you want to remove event '" << it->title << "'? (y/n): ";
                    char confirm;
                    cin >> confirm;

                    if (tolower(confirm) == 'y') {
                        events.erase(it);
                        saveEventsToFile(events);
                        cout << "Event removed successfully.\n";
                    }
                    else {
                        cout << "Removal canceled.\n";
                    }
                    break;
                }
            }

            if (!found) {
                cout << "Event with ID " << eventId << " not found.\n";
            }

            cout << "\nPress Enter to continue...";
            cin.ignore();
            cin.get();
            clearScreen();
            break;
        }
        case 6: {
            clearScreen();
            cout << "===== SYSTEM STATISTICS =====\n\n";

            int adminCount = 0, organizerCount = 0, attendeeCount = 0;
            for (const User& user : users) {
                if (user.role == "admin") adminCount++;
                else if (user.role == "organizer") organizerCount++;
                else if (user.role == "attendee") attendeeCount++;
            }

            cout << "Total Users: " << users.size() << endl;
            cout << " - Admins: " << adminCount << endl;
            cout << " - Organizers: " << organizerCount << endl;
            cout << " - Attendees: " << attendeeCount << endl;
            cout << "\nTotal Events: " << events.size() << endl;

            if (!events.empty()) {
                size_t totalAttendees = 0;
                for (const Event& event : events) {
                    totalAttendees += event.attendees.size();
                }
                double avgAttendees = static_cast<double>(totalAttendees) / events.size();
                cout << "Average attendees per event: " << fixed << setprecision(1) << avgAttendees << endl;
            }
            int upcomingCount = 0, ongoingCount = 0, completedCount = 0, cancelledCount = 0;
            for (const Event& event : events) {
                switch (event.status) {
                case EventStatus::UPCOMING: upcomingCount++; break;
                case EventStatus::ONGOING: ongoingCount++; break;
                case EventStatus::COMPLETED: completedCount++; break;
                case EventStatus::CANCELLED: cancelledCount++; break;
                }
            }

            cout << "\nEvent Status Breakdown:\n";
            cout << " - UPCOMING: " << upcomingCount << endl;
            cout << " - ONGOING: " << ongoingCount << endl;
            cout << " - COMPLETED: " << completedCount << endl;
            cout << " - CANCELLED: " << cancelledCount << endl;

            cout << "\nPress Enter to continue...";
            cin.ignore();
            cin.get();
            clearScreen();
            break;
        }
        case 7: {
            clearScreen();
            cout << "===== MANAGE EVENT STATUS =====\n\n";

            if (events.empty()) {
                cout << "No events available.\n";
                pauseScreen();
                break;
            }

            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(15) << "Status" << setw(15) << "Organizer" << endl;
            cout << string(80, '-') << endl;

            for (const Event& event : events) {
                string organizerName = "Unknown";
                for (const User& user : users) {
                    if (user.id == event.organizerId) {
                        organizerName = user.name;
                        break;
                    }
                }

                cout << setw(5) << event.id
                    << setw(25) << (event.title.length() > 20 ? event.title.substr(0, 20) + "..." : event.title)
                    << setw(12) << event.date
                    << setw(15) << statusToString(event.status)
                    << setw(15) << (organizerName.length() > 12 ? organizerName.substr(0, 12) + "..." : organizerName) << endl;
            }

            
            cout << "\nEnter event ID to change status (0 to cancel): "; 
            int eventId = getIntInput(0, INT_MAX, true); 

            if (eventId == 0) break;

            bool found = false;
            for (Event& event : events) {
                if (event.id == eventId) {
                    found = true;
                    cout << "\nCurrent status: " << statusToString(event.status) << endl;
                    cout << "Select new status:\n";
                    cout << "1. UPCOMING\n";
                    cout << "2. ONGOING\n";
                    cout << "3. COMPLETED\n";
                    cout << "4. CANCELLED\n";
                    cout << "Enter choice (1-4): ";

                    int statusChoice;
                    while (!(cin >> statusChoice) || statusChoice < 1 || statusChoice > 4) {
                        cin.clear();
                        cin.ignore(numeric_limits<streamsize>::max(), '\n');
                        cout << "Invalid input. Please enter 1-4: ";
                    }

                    switch (statusChoice) {
                    case 1: event.status = EventStatus::UPCOMING; break;
                    case 2: event.status = EventStatus::ONGOING; break;
                    case 3: event.status = EventStatus::COMPLETED; break;
                    case 4: event.status = EventStatus::CANCELLED; break;
                    }

                    saveEventsToFile(events);
                    cout << "Event status updated successfully!\n";
                    break;
                }
            }

            if (!found) {
                cout << "Event not found.\n";
            }
            pauseScreen();
            break;
        }

        case 8: {
            clearScreen();
            cout << "===== EVENT RATINGS & COMPLAINTS =====\n\n";

            if (events.empty()) {
                cout << "No events available.\n";
            }
            else {
                for (const Event& ev : events) {
                    cout << "Event ID: " << ev.id << " | " << ev.title << endl;
                    cout << "Average Rating: " << fixed << setprecision(1) << ev.averageRating << "\n";

                    if (ev.ratings.empty()) {
                        cout << "  No ratings or complaints yet.\n";
                    }
                    else {
                        for (const Rating& r : ev.ratings) {
                            cout << "  Rating: " << r.rating
                                << " | Comment: " << r.comment
                                << " | Complaint: " << (r.complaint.empty() ? "-" : r.complaint)
                                << "\n";
                        }
                    }
                    cout << string(80, '-') << endl;
                }
            }
            pauseScreen();
            break;
        }
        case 9:
            cout << "Logging out...\n";
            break;
        }

    } while (choice != 9);
}