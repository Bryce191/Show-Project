#include "organizer.h"
#include "theme.h"
#include "event.h"
#include "payment.h"
#include "helpers.h"
#include "marketing.h"
#include <iostream>
#include <limits>
#include <algorithm>
#include <iomanip>
#include <string>
#include <vector>  

using namespace std;

void organizerMenu(User& organizer) {
    int choice;
    do {
        clearScreen();
        cout << "\n";
        cout << "+=================================================+\n";
        cout << "|                ORGANIZER MENU                   |\n";
        cout << "+=================================================+\n";
        cout << "Welcome, " << organizer.name << " (Organizer)\n\n";
        cout << "+-------------------------------------------------+\n";
        cout << "|  1. Create New Event                            |\n";
        cout << "|  2. Edit My Events                              |\n";
        cout << "|  3. Delete My Events                            |\n";
        cout << "|  4. View My Events                              |\n";
        cout << "|  5. Create / Update Marketing Advertisement     |\n";
        cout << "|  6. Register Attendee for Event                 |\n";
        cout << "|  7. View Ratings & Complaints                   |\n";
        cout << "|  8. View Receipt                                |\n";
        cout << "|  9. LogOut                                      |\n";
        cout << "+=================================================+\n";
        cout << "Enter your choice (1-9): ";
        choice = getIntInput(1, 9);
        clearScreen();

        extern vector<Event> events;
        extern vector<User> users;

        switch (choice) {
        case 1: {
            Event newEvent;
            newEvent.id = generateEventId(events);
            newEvent.organizerId = organizer.id;

            cout << "\n===== CREATE NEW EVENT =====\n";
            cout << "Enter 0 at any time to cancel event creation\n\n";
            cout << "Enter event title: ";
            getline(cin, newEvent.title);
            if (newEvent.title == "0") {
                cout << "Event creation cancelled.\n";
                pauseScreen();
                break;
            }

            cout << "Enter description: ";
            getline(cin, newEvent.description);
            if (newEvent.description == "0") {
                cout << "Event creation cancelled.\n";
                pauseScreen();
                break;
            }

            do {
                cout << "Enter date (YYYY-MM-DD)(2025-01-01 To 2028-12-31): ";
                getline(cin, newEvent.date);
                if (newEvent.date == "0") {
                    cout << "Event creation cancelled.\n";
                    pauseScreen();
                    break;
                }
                if (!isValidDate(newEvent.date)) {
                    cout << "Invalid date format. Try again.(2025-01-01 To 2028-12-31)\n";
                }
            } while (!isValidDate(newEvent.date) && newEvent.date != "0");

            if (newEvent.date == "0") break;

            cout << "\nSelect time slot:\n";
            for (int i = 0; i < Event::slotOptions.size(); i++)
                cout << i + 1 << ". " << Event::slotOptions[i] << endl;
            cout << "Enter choice (1-4, 0 to cancel): ";
            int slotChoice = getIntInput(0, 4);
            if (slotChoice == 0) {
                cout << "Event creation cancelled.\n";
                pauseScreen();
                break;
            }
            newEvent.time = Event::slotOptions[slotChoice - 1];

            cout << "\nSelect location:\n";
            cout << "1. 1st Floor Banquet Hall (RM50)\n";
            cout << "2. 2nd Floor Banquet Hall (RM75)\n";
            cout << "3. 3rd Floor Banquet Hall (RM100)\n";
            cout << "Enter choice (1-3, 0 to cancel): ";
            int locChoice = getIntInput(0, 3);
            if (locChoice == 0) {
                cout << "Event creation cancelled.\n";
                pauseScreen();
                break;
            }

            int venueCost = 0;
            if (locChoice == 1) {
                newEvent.location = "1st Floor Banquet Hall";
                venueCost = 50;
            }
            else if (locChoice == 2) {
                newEvent.location = "2nd Floor Banquet Hall";
                venueCost = 75;
            }
            else {
                newEvent.location = "3rd Floor Banquet Hall";
                venueCost = 100;
            }

            bool conflict = false;
            for (const auto& ev : events) {
                if (ev.date == newEvent.date &&
                    ev.time == newEvent.time &&
                    ev.location == newEvent.location) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) {
                cout << "This slot and location are already taken. Event not created.\n";
                pauseScreen();
                break;
            }

            cin.ignore(numeric_limits<streamsize>::max(), '\n');

            string partInput;
            do {
                cout << "\nEnter expected participants (1-100 only, 0 to cancel): ";
                getline(cin, partInput);
                if (partInput == "0") {
                    cout << "Event creation cancelled.\n";
                    pauseScreen();
                    break;
                }
                try {
                    newEvent.expectedParticipants = stoi(partInput);
                }
                catch (...) {
                    newEvent.expectedParticipants = 0;
                }
            } while ((newEvent.expectedParticipants <= 0 || newEvent.expectedParticipants > 100) && partInput != "0");

            if (partInput == "0") break;

            cout << "\nDo you want to add Theme & Decoration Planner? (y/n, 0 to cancel): ";
            char themeChoice = getYesNoInput();
            if (themeChoice == '0') {
                cout << "Event creation cancelled.\n";
                pauseScreen();
                break;
            }

            double themeCost = 0;
            string themeName, vendorName;

            if (themeChoice == 'y' || themeChoice == 'Y') {
                themeCost = themeMenu(themeName, vendorName);
                // You might want to add cancellation support in themeMenu too
                newEvent.themeName = themeName.empty() ? "None" : themeName;
                newEvent.vendorName = vendorName.empty() ? "None" : vendorName;
            }
            else {
                newEvent.themeName = "None";
                newEvent.vendorName = "None";
            }
            newEvent.themeCost = themeCost;

            newEvent.totalFee = calculateTotalFee(venueCost, newEvent.expectedParticipants, themeCost);

            // Only save if payment is successful
            if (ProcessPayment(newEvent.totalFee)) {
                events.push_back(newEvent);
                saveEventsToFile(events);
                cout << "\nBooking completed successfully!\n";
            }
            else {
                cout << "\nPayment failed or cancelled. Event was not created.\n";
            }

            pauseScreen();
            break;
        }

        case 2: {
            clearScreen();
            cout << "===== EDIT MY EVENTS =====\n\n";

            bool hasEvents = false;
            for (const Event& event : events) {
                if (event.organizerId == organizer.id) {
                    hasEvents = true;
                    break;
                }
            }

            if (!hasEvents) {
                cout << "You haven't created any events yet.\n";
                cout << "\nPress Enter to continue...";
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
                cin.get();
                clearScreen();
                break;
            }

            cout << "Your Events:\n";
            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(30) << "Time" << setw(30) << "Location" << endl;
            cout << string(100, '-') << endl;

            for (const Event& event : events) {
                if (event.organizerId == organizer.id) {
                    cout << setw(5) << event.id
                        << setw(25) << event.title.substr(0, 20)
                        << setw(12) << event.date
                        << setw(30) << event.time
                        << setw(30) << event.location.substr(0, 25)
                        << endl;
                }
            }

            cout << "\nEnter ID of event to edit (0 to exit): ";
            int eventId = getIntInput(0, INT_MAX, true); // Allow 0 to exit

            if (eventId == 0) {
                cout << "\nReturning to organizer menu...\n";
                pauseScreen();
                break;
            }

            bool found = false;
            for (Event& event : events) {
                if (event.id == eventId && event.organizerId == organizer.id) {
                    found = true;
                    double oldFee = event.totalFee;

                    cout << "\nCurrent Details:\n";
                    cout << "Title: " << event.title << endl;
                    cout << "Description: " << event.description << endl;
                    cout << "Date: " << event.date << endl;
                    cout << "Time: " << event.time << endl;
                    cout << "Location: " << event.location << endl;
                    cout << "Expected Participants: " << event.expectedParticipants << endl;
                    cout << "Theme Planner: "
                        << (event.themeName == "None" ? "None" : event.themeName + " (Vendor: " + event.vendorName + ")") << endl;
                    cout << "Theme Cost: RM " << fixed << setprecision(2) << event.themeCost << endl;
                    cout << "Total Fee: RM " << fixed << setprecision(2) << event.totalFee << endl;

                    cout << "\nEnter new details (leave blank to keep current):\n";
                    cin.ignore(numeric_limits<streamsize>::max(), '\n'); 

                    string input;
                    cout << "Title [" << event.title << "]: ";
                    getline(cin, input);
                    if (!input.empty()) event.title = input;

                    cout << "Description [" << event.description << "]: ";
                    getline(cin, input);
                    if (!input.empty()) event.description = input;

                    do {
                        cout << "Date [" << event.date << "]: ";
                        getline(cin, input);
                        if (!input.empty()) {
                            if (isValidDate(input)) {
                                event.date = input;
                                break;
                            }
                            else {
                                cout << "Invalid date format. Please use YYYY-MM-DD.\n";
                            }
                        }
                        else break;
                    } while (true);

                    cout << "\nSelect time slot (leave blank to keep current):\n";
                    for (int i = 0; i < Event::slotOptions.size(); i++)
                        cout << i + 1 << ". " << Event::slotOptions[i] << endl;

                    string timeInput;
                    getline(cin, timeInput);
                    if (!timeInput.empty()) {
                        int slotChoice = stoi(timeInput);
                        if (slotChoice >= 1 && slotChoice <= Event::slotOptions.size()) {
                            string newTime = Event::slotOptions[slotChoice - 1];
                            bool conflict = false;
                            for (const auto& ev : events) {
                                if (ev.id != event.id &&
                                    ev.date == event.date &&
                                    ev.time == newTime &&
                                    ev.location == event.location) {
                                    conflict = true;
                                    break;
                                }
                            }
                            if (conflict) {
                                cout << "Conflict: Another event is already scheduled at this time & location.\n";
                            }
                            else {
                                event.time = newTime;
                            }
                        }
                    }

                    cout << "\nSelect location (leave blank to keep current):\n";
                    cout << "1. 1st Floor Banquet Hall\n";
                    cout << "2. 2nd Floor Banquet Hall\n";
                    cout << "3. 3rd Floor Banquet Hall\n";
                    cout << "Choice: ";
                    string locInput;
                    getline(cin, locInput);
                    if (!locInput.empty()) {
                        switch (stoi(locInput)) {
                        case 1: event.location = "1st Floor Banquet Hall"; break;
                        case 2: event.location = "2nd Floor Banquet Hall"; break;
                        case 3: event.location = "3rd Floor Banquet Hall"; break;
                        }
                    }

                    cout << "\nExpected Participants [" << event.expectedParticipants << "]: ";
                    getline(cin, input);
                    if (!input.empty()) {
                        try {
                            int num = stoi(input);
                            if (num > 0 && num <= 100) {
                                event.expectedParticipants = num;
                            }
                            else {
                                cout << "Invalid number. Must be between 1 and 100.\n";
                            }
                        }
                        catch (...) {
                            cout << "Invalid input.\n";
                        }
                    }

                    cout << "\nChange Theme & Decoration Planner? (y/n): ";
                    char themeChangeChoice = getYesNoInput();
                    string themeName = event.themeName;
                    string vendorName = event.vendorName;

                    if (themeChangeChoice == 'y' || themeChangeChoice == 'Y') {
                        double newThemeCost = themeMenu(themeName, vendorName);

                        if (themeName.empty() || vendorName.empty()) {
                            cout << "You selected 'No decoration package'.\n";
                            event.themeName = "None";
                            event.vendorName = "None";
                            event.themeCost = 0.0;
                        }
                        else {
                            event.themeName = themeName;
                            event.vendorName = vendorName;
                            event.themeCost = newThemeCost;
                        }
                    }

                    auto getVenueCost = [](const string& location) {
                        if (location.find("1st") != string::npos) return 50;
                        if (location.find("2nd") != string::npos) return 75;
                        if (location.find("3rd") != string::npos) return 100;
                        return 0;
                        };

                    int venueCost = getVenueCost(event.location);
                    event.totalFee = calculateTotalFee(venueCost, event.expectedParticipants, event.themeCost);

                    cout << "\nUpdated Total Fee: RM " << fixed << setprecision(2) << event.totalFee << endl;

                    if (event.totalFee > oldFee) {
                        double extra = event.totalFee - oldFee;
                        cout << "\nThe event cost increased. You need to pay an extra RM" << extra << ".\n";

                        // Only proceed if extra payment succeeds
                        if (ProcessPayment(extra)) {
                            saveEventsToFile(events);
                            cout << "\nEvent updated successfully!\n";
                        }
                        else {
                            // revert changes if payment failed
                            event.totalFee = oldFee;
                            cout << "\nPayment failed or cancelled. Event changes were not saved.\n";
                        }
                    }
                    else {
                        saveEventsToFile(events);
                        cout << "\nNo extra payment required. Event updated successfully!\n";
                    }
                    break;
                }
            }

            if (!found) {
                cout << "Event not found or you don't have permission to edit it.\n";
            }
            cout << "\nPress Enter to continue...";
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cin.get();
            clearScreen();
            break;
        }

        case 3: {
            clearScreen();
            cout << "===== DELETE MY EVENTS =====\n\n";

            // Check if user has events
            bool hasEvents = false;
            for (const Event& event : events) {
                if (event.organizerId == organizer.id) {
                    hasEvents = true;
                    break;
                }
            }

            if (!hasEvents) {
                cout << "You haven't created any events yet.\n";
                cout << "\nPress Enter to continue...";
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
                cin.get();
                break;
            }

            cout << left << setw(5) << "ID"
                << setw(25) << "Title"
                << setw(12) << "Date"
                << setw(25) << "Slot"
                << setw(25) << "Location" << endl;
            cout << string(95, '-') << endl;

            for (const Event& event : events) {
                if (event.organizerId == organizer.id) {
                    string slotDisplay = event.time.empty() ? "-" : event.time;

                    cout << left << setw(5) << event.id
                        << setw(25) << (event.title.empty() ? "(No Title)" : event.title.substr(0, 20))
                        << setw(12) << (event.date.empty() ? "-" : event.date)
                        << setw(25) << slotDisplay
                        << setw(25) << (event.location.empty() ? "-" : event.location.substr(0, 22))
                        << endl;
                }
            }

            cout << "\nEnter ID of event to delete (0 to cancel): ";
            int eventId = getIntInput(0, INT_MAX, true); 

            if (eventId == 0) {
                cout << "\nDeletion canceled.\n";
                pauseScreen();
                break;
            }

            bool found = false;
            for (auto it = events.begin(); it != events.end(); ++it) {
                if (it->id == eventId && it->organizerId == organizer.id) {
                    found = true;

                    cout << "Are you sure you want to delete '"
                        << (it->title.empty() ? "(No Title)" : it->title)
                        << "'? (y/n): ";
                    char confirm;
                    cin >> confirm;

                    if (tolower(confirm) == 'y') {
                        events.erase(it);
                        saveEventsToFile(events);
                        cout << "Event deleted successfully.\n";
                    }
                    else {
                        cout << "Deletion canceled.\n";
                    }
                    break;
                }
            }

            if (!found) {
                cout << "Event not found or you don't have permission to delete it.\n";
            }

            pauseScreen();
            clearScreen();
            break;
        }

        case 4: { 
            clearScreen();
            cout << "===== MY EVENTS =====\n\n";

            bool hasEvents = false;
            for (const Event& event : events) {
                if (event.organizerId == organizer.id) {
                    hasEvents = true;
                    break;
                }
            }

            if (!hasEvents) {
                cout << "You haven't created any events yet.\n";
                pauseScreen();
                clearScreen();
                break;
            }

            cout << setw(5) << "ID"
                << setw(25) << "Title"
                << setw(12) << "Date"
                << setw(25) << "Slot"
                << setw(20) << "Location"
                << setw(25) << "Description"
                << setw(15) << "Participants"
                << endl;

            cout << string(130, '-') << endl;

            for (const Event& event : events) {
                if (event.organizerId == organizer.id) {
                    string slotDisplay = event.time.empty() ? "-" : event.time;

                    cout << setw(5) << event.id
                        << setw(25) << (event.title.empty() ? "(No Title)" : event.title.substr(0, 20))
                        << setw(12) << (event.date.empty() ? "-" : event.date)
                        << setw(25) << slotDisplay
                        << setw(20) << (event.location.empty() ? "-" : event.location.substr(0, 18))
                        << setw(25) << (event.description.empty() ? "-" : event.description.substr(0, 25))
                        << setw(15) << event.expectedParticipants
                        << endl;
                }
            }

            cout << "\nEnter an event ID to view details (or 0 to go back): ";
            int eventId = getIntInput(0, INT_MAX, true); 

            if (eventId == 0) break;

           
                bool found = false;
                for (const Event& event : events) {
                    if (event.id == eventId && event.organizerId == organizer.id) {
                        found = true;
                        clearScreen();
                        cout << "===== EVENT DETAILS =====\n\n";
                        cout << "ID: " << event.id << endl;
                        cout << "Title: " << (event.title.empty() ? "-" : event.title) << endl;
                        cout << "Description: " << (event.description.empty() ? "-" : event.description) << endl;
                        cout << "Date: " << (event.date.empty() ? "-" : event.date) << endl;
                        cout << "Time Slot: " << (event.time.empty() ? "-" : event.time) << endl;
                        cout << "Location: " << (event.location.empty() ? "-" : event.location) << endl;
                        cout << "Expected Participants: " << event.expectedParticipants << endl;

                        if (event.themeName.empty() || event.themeName == "None") {
                            cout << "Theme Planner: None\n";
                            cout << "Theme Cost: RM0.00\n";
                        }
                        else {
                            cout << "Theme Planner: " << event.themeName
                                << " (Vendor: " << event.vendorName << ")\n";
                            cout << "Theme Cost: RM"
                                << fixed << setprecision(2)
                                << event.themeCost << endl;
                        }

                        int venueCost = 0;
                        if (event.location.find("1st Floor") != string::npos)
                            venueCost = 50;
                        else if (event.location.find("2nd Floor") != string::npos)
                            venueCost = 75;
                        else if (event.location.find("3rd Floor") != string::npos)
                            venueCost = 100;

                        double totalFee = venueCost
                            + (event.expectedParticipants * 5.0)
                            + event.themeCost;
                        cout << "Venue Cost: RM" << venueCost << endl;
                        cout << "Total Fee: RM"
                            << fixed << setprecision(2)
                            << totalFee << endl;

                        cout << "Marketing: "
                            << (event.marketing.empty() ? "-" : event.marketing)
                            << endl;

                        cout << "\nAttendees (" << event.attendees.size() << "):\n";
                        if (event.attendees.empty()) {
                            cout << "No attendees yet.\n";
                        }
                        else {
                            cout << setw(5) << "ID"
                                << setw(20) << "Name"
                                << setw(25) << "Email" << endl;
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
                    cout << "Event not found or you don't have permission to view it.\n";
                }
                pauseScreen();
                clearScreen();
            
            break;
        }

        case 5: {
            createAdvertisement(organizer, events);
            break;
        }

        case 6: {
            clearScreen();
            cout << "===== REGISTER ATTENDEE FOR EVENT =====\n\n";

            if (events.empty()) {
                cout << "No events available at the moment.\n";
                pauseScreen();
                break;
            }

            string attendeeUsername;
            cout << "Enter attendee username to register: ";
            cin >> attendeeUsername;

            User* attendee = nullptr;
            for (User& user : users) {
                if (user.username == attendeeUsername && user.role == "attendee") {
                    attendee = &user;
                    break;
                }
            }

            if (!attendee) {
                cout << "Attendee not found or invalid username.\n";
                pauseScreen();
                break;
            }

            // Show attendee details for confirmation
            cout << "\n===== ATTENDEE DETAILS =====\n";
            cout << "Name: " << attendee->name << endl;
            cout << "Email: " << attendee->email << endl;
            cout << "Username: " << attendee->username << endl;
            cout << "============================\n\n";

            cout << "Confirm this is the correct attendee? (y/n): ";
            char confirmAttendee = getYesNoInput();
            if (tolower(confirmAttendee) != 'y') {
                cout << "Operation cancelled.\n";
                pauseScreen();
                break;
            }

            cout << "\n===== YOUR EVENTS =====\n\n";
            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(10) << "Time" << setw(20) << "Location" << endl;
            cout << string(80, '-') << endl;

            bool hasEvents = false;
            for (const Event& event : events) {
                if (event.organizerId == organizer.id && event.status == EventStatus::UPCOMING) {
                    hasEvents = true;
                    cout << setw(5) << event.id << setw(25) << event.title.substr(0, 20)
                        << setw(12) << event.date << setw(10) << event.time
                        << setw(20) << event.location.substr(0, 15) << endl;
                }
            }

            if (!hasEvents) {
                cout << "You haven't created any UPCOMING events yet.\n";
                pauseScreen();
                break;
            }

            int eventId;
            cout << "\nEnter event ID to register " << attendee->name << " for: ";
            while (!(cin >> eventId)) {
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
                cout << "Invalid input. Please enter a numeric ID: ";
            }

            bool eventFound = false;
            for (Event& event : events) {
                if (event.id == eventId && event.organizerId == organizer.id) {
                    eventFound = true;

                    auto it = find(event.attendees.begin(), event.attendees.end(), attendee->id);
                    if (it != event.attendees.end()) {
                        cout << attendee->name << " is already registered for '" << event.title << "'.\n";
                    }
                    else {
                        // Show final confirmation
                        cout << "\nRegister " << attendee->name << " for event: " << event.title << endl;
                        cout << "Date: " << event.date << " at " << event.time << endl;
                        cout << "Location: " << event.location << endl;
                        cout << "\nConfirm registration? (y/n): ";

                        char confirmFinal = getYesNoInput();
                        if (tolower(confirmFinal) == 'y') {
                            event.attendees.push_back(attendee->id);
                            saveEventsToFile(events);
                            cout << "Successfully registered " << attendee->name << " for '" << event.title << "'!\n";
                        }
                        else {
                            cout << "Registration cancelled.\n";
                        }
                    }
                    break;
                }
            }

            if (!eventFound) {
                cout << "Event not found or you don't have permission to modify it.\n";
            }

            pauseScreen();
            break;
        }
        case 7: {
            clearScreen();
            cout << "===== EVENT RATINGS & COMPLAINTS =====\n\n";

            bool hasEvents = false;
            for (const Event& ev : events) {
                if (ev.organizerId == organizer.id) {
                    hasEvents = true;
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

            if (!hasEvents) {
                cout << "You haven't created any events yet.\n";
            }

            pauseScreen();
            break;
        }

        case 8: {
            clearScreen();
            cout << "===== VIEW RECEIPT =====\n\n";

            bool hasEvents = false;
            for (const Event& ev : events) {
                if (ev.organizerId == organizer.id) {
                    hasEvents = true;
                    cout << "Event ID: " << ev.id << " | " << ev.title << endl;
                }
            }

            if (!hasEvents) {
                cout << "You haven't created any events yet.\n";
                pauseScreen();
                break;
            }

            cout << "\nEnter Event ID to view receipt (0 to cancel): ";
            int eventId = getIntInput(0, INT_MAX, true);
            if (eventId == 0) {
                cout << "Cancelled.\n";
                pauseScreen();
                break;
            }

            bool found = false;
            for (const Event& ev : events) {
                if (ev.id == eventId && ev.organizerId == organizer.id) {
                    found = true;
                    clearScreen();

                    cout << "=====================================\n";
                    cout << "              EVENT RECEIPT          \n";
                    cout << "=====================================\n";
                    cout << "Receipt No. : R" << ev.id << "2025\n";
                    cout << "Organizer   : " << organizer.name << endl;
                    cout << "Event Title : " << ev.title << endl;
                    cout << "Date & Time : " << ev.date << " | " << ev.time << endl;
                    cout << "Location    : " << ev.location << endl;
                    cout << "Theme       : " << ev.themeName << endl;
                    cout << "Participants: " << ev.expectedParticipants << endl;
                    cout << "-------------------------------------\n";

                    // Calculate costs
                    int venueCost = 0;
                    if (ev.location.find("1st Floor") != string::npos)
                        venueCost = 50;
                    else if (ev.location.find("2nd Floor") != string::npos)
                        venueCost = 75;
                    else if (ev.location.find("3rd Floor") != string::npos)
                        venueCost = 100;

                    double participantCost = ev.expectedParticipants * 5.0;
                    double themeCost = ev.themeCost;
                    double totalFee = venueCost + participantCost + themeCost;

                    cout << "Venue Cost        : RM" << fixed << setprecision(2) << venueCost << endl;
                    cout << "Participant Cost  : RM" << fixed << setprecision(2) << participantCost << endl;
                    cout << "Theme & Decoration: RM" << fixed << setprecision(2) << themeCost << endl;
                    cout << "-------------------------------------\n";
                    cout << "TOTAL PAID        : RM" << fixed << setprecision(2) << totalFee << endl;
                    cout << "=====================================\n";
                    cout << "   Payment Status : SUCCESSFUL       \n";
                    cout << "=====================================\n";
                    cout << "        THANK YOU FOR BOOKING        \n";
                    cout << "=====================================\n";

                    break;
                }
            }

            if (!found) {
                cout << "Event not found or you don't have permission to view it.\n";
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