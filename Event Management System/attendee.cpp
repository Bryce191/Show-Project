#include "attendee.h"
#include "marketing.h"
#include <iomanip>
#include <algorithm>
#include <climits> 
#include "helpers.h"
#include "user.h"
#include "event.h"

using namespace std;

extern vector<User> users;
extern vector<Event> events;

void attendeeMenu(User& attendee) {
    int choice;
    do {
        clearScreen();
        cout << "\n";
        cout << "+=================================================+\n";
        cout << "|                 ATTENDEE MENU                   |\n";
        cout << "+=================================================+\n";
        cout << "Welcome, " << attendee.name << " (Attendee)\n\n";
        cout << "Advertisement:\n";
        displayTopEvent(events);

        cout << "+-------------------------------------------------+\n";
        cout << "|  1. Browse Events                               |\n";
        cout << "|  2. Register for an Event                       |\n";
        cout << "|  3. Cancel Registration                         |\n";
        cout << "|  4. My Registered Events                        |\n";
        cout << "|  5. Rate Event                                  |\n";
        cout << "|  6. Logout                                      |\n";
        cout << "+=================================================+\n";
        cout << "Enter your choice (1-6): ";

        while (!(cin >> choice) || choice < 1 || choice > 6) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input. Please enter a number between 1 and 6: ";
        }

        switch (choice) {
        case 1: {
            clearScreen();
            cout << "===== BROWSE EVENTS =====\n\n";

            if (events.empty()) {
                cout << "No events available at the moment.\n";
                pauseScreen();
                break;
            }

            cout << "Filter by status:\n";
            cout << "1. All Events\n";
            cout << "2. UPCOMING\n";
            cout << "3. ONGOING\n";
            cout << "4. COMPLETED\n";
            cout << "5. CANCELLED\n";
            cout << "Enter choice (1-5): ";

            int filterChoice = getIntInput(1, 5);
            EventStatus filterStatus;
            bool showAll = true;

            if (filterChoice == 2) {
                filterStatus = EventStatus::UPCOMING;
                showAll = false;
            }
            else if (filterChoice == 3) {
                filterStatus = EventStatus::ONGOING;
                showAll = false;
            }
            else if (filterChoice == 4) {
                filterStatus = EventStatus::COMPLETED;
                showAll = false;
            }
            else if (filterChoice == 5) {
                filterStatus = EventStatus::CANCELLED;
                showAll = false;
            }

            clearScreen();
            cout << "===== BROWSE EVENTS =====\n\n";

            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(10) << "Time" << setw(20) << "Location" << setw(12) << "Status"
                << setw(15) << "Organizer" << setw(10) << "Attendees" << endl;
            cout << string(120, '-') << endl;

            bool hasEventsToShow = false;

            for (const Event& event : events) {
                if (!showAll && event.status != filterStatus) {
                    continue;
                }

                hasEventsToShow = true;
                string organizerName = "Unknown";
                for (const User& user : users) {
                    if (user.id == event.organizerId) {
                        organizerName = user.name;
                        break;
                    }
                }

                bool isRegistered = find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end();

                cout << setw(5) << event.id << setw(25) << (event.title.length() > 20 ? event.title.substr(0, 20) + "..." : event.title)
                    << setw(12) << event.date << setw(10) << event.time
                    << setw(20) << (event.location.length() > 15 ? event.location.substr(0, 15) + "..." : event.location)
                    << setw(12) << statusToString(event.status)
                    << setw(15) << (organizerName.length() > 12 ? organizerName.substr(0, 12) + "..." : organizerName)
                    << setw(10) << event.attendees.size();

                if (isRegistered) {
                    cout << " (Registered)";
                }
                cout << endl;
            }

            if (!hasEventsToShow) {
                cout << "No events match the selected filter.\n";
                pauseScreen();
                break;
            }

            cout << "\nEnter an event ID to view details (or 0 to go back): ";
            int eventId = getIntInput(0, INT_MAX, true);

            if (eventId == 0) break;

          
            bool found = false;
            for (const Event& event : events) {
                if (event.id == eventId) {
                    found = true;
                    clearScreen();
                    cout << "===== EVENT DETAILS =====\n\n";
                    cout << "Title: " << event.title << endl;
                    cout << "Description: |" << event.description << endl;
                    cout << "Date: " << event.date << endl;
                    cout << "Time: " << event.time << endl;
                    cout << "Location: " << event.location << endl;
                    cout << "Status: " << statusToString(event.status) << endl;

                    string organizerName = "Unknown";
                    for (const User& user : users) {
                        if (user.id == event.organizerId) {
                            organizerName = user.name;
                            break;
                        }
                    }
                    cout << "Organizer: " << organizerName << endl;
                    cout << "Attendees: " << event.attendees.size() << endl;

                    if (event.status == EventStatus::COMPLETED && event.averageRating > 0) {
                        cout << "Average Rating: " << fixed << setprecision(1) << event.averageRating  << endl;
                    }

                    if (!event.marketing.empty()) {
                        cout << "\n*** Advertisement ***\n";
                        cout << event.marketing << endl;
                        cout << "*********************\n";
                    }

                    bool isRegistered = find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end();
                    if (isRegistered) {
                        cout << "\nYou are registered for this event.\n";
                    }
                    else {
                        cout << "\nYou are not registered for this event.\n";
                    }
                    break;
                }
            } 

            if (!found) {
                cout << "Event not found.\n";
            }
            pauseScreen();
           
            break;
        }
        case 2: {
            clearScreen();
            cout << "===== REGISTER FOR EVENT =====\n\n";

            if (events.empty()) {
                cout << "No events available at the moment.\n";
                pauseScreen();
                break;
            }

            cout << "Available UPCOMING Events:\n";
            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(10) << "Time" << setw(20) << "Location" << setw(12) << "Status" << endl;
            cout << string(90, '-') << endl;

            bool hasAvailableEvents = false;
            for (const Event& event : events) {
                bool isRegistered = find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end();
                if (!isRegistered && event.status == EventStatus::UPCOMING) {
                    hasAvailableEvents = true;
                    cout << setw(5) << event.id << setw(25) << (event.title.length() > 20 ? event.title.substr(0, 20) + "..." : event.title)
                        << setw(12) << event.date << setw(10) << event.time
                        << setw(20) << (event.location.length() > 15 ? event.location.substr(0, 15) + "..." : event.location)
                        << setw(12) << statusToString(event.status) << endl;
                }
            }

            if (!hasAvailableEvents) {
                cout << "No available UPCOMING events to register for.\n";
                pauseScreen();
                break;
            }

            cout << "\nEnter ID of event to register (or 0 to cancel): ";
            int eventId = getIntInput(0, INT_MAX, true);

            if (eventId == 0) {
                cout << "Registration cancelled.\n";
                pauseScreen();
                break;
            }

            bool found = false;
            for (Event& event : events) {
                if (event.id == eventId) {
                    found = true;

                    if (event.status != EventStatus::UPCOMING) {
                        cout << "Cannot register for events that are not UPCOMING.\n";
                        break;
                    }

                    bool isRegistered = find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end();
                    if (isRegistered) {
                        cout << "You are already registered for '" << event.title << "'.\n";
                    }
                    else {
                        // Show confirmation
                        cout << "\nEvent: " << event.title << endl;
                        cout << "Date: " << event.date << " at " << event.time << endl;
                        cout << "Location: " << event.location << endl;
                        cout << "\nConfirm registration? (y/n): ";

                        char confirm = getYesNoInput();
                        if (tolower(confirm) == 'y') {
                            event.attendees.push_back(attendee.id);
                            saveEventsToFile(events);
                            cout << "Successfully registered for '" << event.title << "'!\n";
                        }
                        else {
                            cout << "Registration cancelled.\n";
                        }
                    }
                    break;
                }
            }

            if (!found) {
                cout << "Event not found.\n";
            }
            pauseScreen();
            break;
        }
        case 3: {
            clearScreen();
            cout << "===== CANCEL REGISTRATION =====\n\n";

            bool hasRegistrations = false;
            for (const Event& event : events) {
                if (find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end()) {
                    hasRegistrations = true;
                    break;
                }
            }

            if (!hasRegistrations) {
                cout << "You haven't registered for any events yet.\n";
                cout << "\nPress Enter to continue...";
                cin.ignore();
                cin.get();
                clearScreen();
                break;
            }

            cout << "Your Registered Events:\n";
            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(10) << "Time" << setw(20) << "Location" << setw(12) << "Status" << endl;
            cout << string(90, '-') << endl;

            for (const Event& event : events) {
                if (find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end()) {
                    cout << setw(5) << event.id << setw(25) << (event.title.length() > 20 ? event.title.substr(0, 20) + "..." : event.title)
                        << setw(12) << event.date << setw(10) << event.time
                        << setw(20) << (event.location.length() > 15 ? event.location.substr(0, 15) + "..." : event.location)
                        << setw(12) << statusToString(event.status) << endl;
                }
            }

            cout << "\nEnter ID of event to cancel registration (or 0 to cancel): ";
            int eventId = getIntInput(0, INT_MAX, true); // Allow 0 to exit

            if (eventId == 0) {
                cout << "Cancellation cancelled.\n";
                pauseScreen();
                break;
            }

            
                bool found = false;
                for (Event& event : events) {
                    if (event.id == eventId) {
                        found = true;
                        auto it = find(event.attendees.begin(), event.attendees.end(), attendee.id);
                        if (it != event.attendees.end()) {
                            event.attendees.erase(it);
                            saveEventsToFile(events);
                            cout << "Registration canceled for '" << event.title << "'.\n";
                        }
                        else {
                            cout << "You are not registered for this event.\n";
                        }
                        break;
                    }
                }

                if (!found) {
                    cout << "Event not found.\n";
                }
                cout << "\nPress Enter to continue...";
                cin.ignore();
                cin.get();
                clearScreen();
            
            break;
        }
        case 4: {
            clearScreen();
            cout << "===== MY REGISTERED EVENTS =====\n\n";

            bool hasRegistrations = false;
            for (const Event& event : events) {
                if (find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end()) {
                    hasRegistrations = true;
                    break;
                }
            }

            if (!hasRegistrations) {
                cout << "You haven't registered for any events yet.\n";
                cout << "\nPress Enter to continue...";
                cin.ignore();
                cin.get();
                clearScreen();
                break;
            }

            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date"
                << setw(10) << "Time" << setw(20) << "Location" << setw(12) << "Status"
                << setw(15) << "Organizer" << endl;
            cout << string(110, '-') << endl;

            for (const Event& event : events) {
                if (find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end()) {
                    string organizerName = "Unknown";
                    for (const User& user : users) {
                        if (user.id == event.organizerId) {
                            organizerName = user.name;
                            break;
                        }
                    }

                    cout << setw(5) << event.id << setw(25) << (event.title.length() > 20 ? event.title.substr(0, 20) + "..." : event.title)
                        << setw(12) << event.date << setw(10) << event.time
                        << setw(20) << (event.location.length() > 15 ? event.location.substr(0, 15) + "..." : event.location)
                        << setw(12) << statusToString(event.status)
                        << setw(15) << (organizerName.length() > 12 ? organizerName.substr(0, 12) + "..." : organizerName) << endl;
                }
            }

            cout << "\nEnter an event ID to view details (or 0 to go back): ";
            int eventId = getIntInput(0, INT_MAX, true); 

            if (eventId == 0) break;

            
                bool found = false;
                for (const Event& event : events) {
                    if (event.id == eventId && find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end()) {
                        found = true;
                        clearScreen();
                        cout << "===== EVENT DETAILS =====\n\n";
                        cout << "Title: " << event.title << endl;
                        cout << "Description: " << event.description << endl;
                        cout << "Date: " << event.date << endl;
                        cout << "Time: " << event.time << endl;
                        cout << "Location: " << event.location << endl;
                        cout << "Status: " << statusToString(event.status) << endl;

                        string organizerName = "Unknown";
                        for (const User& user : users) {
                            if (user.id == event.organizerId) {
                                organizerName = user.name;
                                break;
                            }
                        }
                        cout << "Organizer: " << organizerName << endl;

                        //show advertisement also in "My Registered Events"
                        if (!event.marketing.empty()) {
                            cout << "\n*** Advertisement ***\n";
                            cout << event.marketing << endl;
                            cout << "*********************\n";
                        }

                        break;
                    }
                }

                if (!found) {
                    cout << "Event not found or you're not registered for it.\n";
                }
                cout << "\nPress Enter to continue...";
                cin.ignore();
                cin.get();
                clearScreen();
            
            break;
        }
        case 5: {
            clearScreen();
            cout << "===== RATE AND COMPLAINT =====\n\n";


            vector<Event*> completedEvents;
            for (Event& event : events) {
                if (event.status == EventStatus::COMPLETED &&
                    find(event.attendees.begin(), event.attendees.end(), attendee.id) != event.attendees.end()) {
                    completedEvents.push_back(&event);
                }
            }

            if (completedEvents.empty()) {
                cout << "No completed events available for rating.\n";
                pauseScreen();
                break;
            }


            cout << setw(5) << "ID" << setw(25) << "Title" << setw(12) << "Date" << setw(10) << "Rating" << endl;
            cout << string(60, '-') << endl;
            for (const Event* event : completedEvents) {
                double userRating = 0;
                bool hasRated = false;
                for (const Rating& rating : event->ratings) {
                    if (rating.attendeeId == attendee.id) {
                        userRating = rating.rating;
                        hasRated = true;
                        break;
                    }
                }
                cout << setw(5) << event->id
                    << setw(25) << (event->title.length() > 20 ? event->title.substr(0, 20) + "..." : event->title)
                    << setw(12) << event->date
                    << setw(10);
                if (hasRated) cout << fixed << setprecision(1) << userRating;
                else cout << "Not rated";
                cout << endl;
            }

            cout << "\nEnter event ID to rate (0 to cancel): ";
            int eventId = getIntInput(0, INT_MAX, true); 

            if (eventId == 0) break;

            Event* targetEvent = nullptr;
            for (Event* event : completedEvents) {
                if (event->id == eventId) {
                    targetEvent = event;
                    break;
                }
            }

            if (!targetEvent) {
                cout << "Event not found or you did not attend it.\n";
                pauseScreen();
                break;
            }


            Rating userRatingEntry;
            bool hasRated = false;
            for (Rating& rating : targetEvent->ratings) {
                if (rating.attendeeId == attendee.id) {
                    userRatingEntry = rating;
                    hasRated = true;
                    break;
                }
            }

            if (hasRated) {
                cout << "You already rated this event: " << userRatingEntry.rating << " stars\n";
                cout << "Comment: " << userRatingEntry.comment << "\n";
                cout << "Complaint: " << userRatingEntry.complaint << "\n";
                cout << "\nDo you want to update your rating? (y/n): ";
                char update = getYesNoInput();
                if (update == 'n' || update == 'N') {
                    pauseScreen();
                    break;
                }
            }

            cout << "Rate this event (1.0 - 5.0 stars): ";
            while (!(cin >> userRatingEntry.rating) || userRatingEntry.rating < 1.0 || userRatingEntry.rating > 5.0) {
                cin.clear();
                cin.ignore(numeric_limits<streamsize>::max(), '\n');
                cout << "Please enter a number between 1.0 and 5.0: ";
            }
            cin.ignore();

            cout << "Enter your comment: ";
            getline(cin, userRatingEntry.comment);

            cout << "Enter your complaint (if any): ";
            getline(cin, userRatingEntry.complaint);

            userRatingEntry.attendeeId = attendee.id;

            bool updated = false;
            for (Rating& rating : targetEvent->ratings) {
                if (rating.attendeeId == attendee.id) {
                    rating = userRatingEntry;
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                targetEvent->ratings.push_back(userRatingEntry);
            }

            double total = 0;
            for (const Rating& r : targetEvent->ratings) total += r.rating;
            targetEvent->averageRating = total / targetEvent->ratings.size();

            saveEventsToFile(events);
            cout << "Thank you for your feedback!\n";
            pauseScreen();
            break;
        }
        case 6:
            return;
        }
    } while (choice != 6);
}