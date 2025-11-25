#include "marketing.h"
#include <iostream>
#include <algorithm>
#include <string>

using namespace std;

void displayTopEvent(const vector<Event>& events) {
    if (!events.empty()) {
        // Find the event with the most attendees
        auto topEvent = max_element(events.begin(), events.end(),
            [](const Event& a, const Event& b) {
                return a.attendees.size() < b.attendees.size();
            });

        string title = topEvent->title;
        size_t count = topEvent->attendees.size();

        // Create a properly formatted display
        string content = "Today's Top Event: " + title + " (Attendees: " + to_string(count) + ")";

        // Calculate border length based on content
        size_t borderLength = content.size() + 4; // +4 for spaces and border chars

        cout << string(borderLength, '=') << "\n";
        cout << "| " << content << " |\n";
        cout << string(borderLength, '=') << "\n";

        // Show advertisement if available
        if (!topEvent->marketing.empty()) {
            cout << topEvent->marketing << "\n";
            cout << string(borderLength, '-') << "\n";
        }
        cout << "\n";
    }
    else {
        string content = "Today's Top Event: No events available yet";
        size_t borderLength = content.size() + 4;
        cout << string(borderLength, '=') << "\n";
        cout << "| " << content << " |\n";
        cout << string(borderLength, '=') << "\n\n";
    }
}

void createAdvertisement(User& organizer, vector<Event>& events) {
    cout << "===== CREATE / UPDATE ADVERTISEMENT =====\n\n";

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
        return;
    }

    cout << "Your Events:\n";
    cout << string(60, '-') << "\n";
    for (const Event& event : events) {
        if (event.organizerId == organizer.id) {
            cout << "ID: " << event.id << " | " << event.title << "\n";
            cout << "Current Advertisement: " << (event.marketing.empty() ? "[None]" : event.marketing) << "\n";
            cout << string(60, '-') << "\n";
        }
    }

    int eventId;
    cout << "\nEnter the ID of the event to update advertisement (0 to exit): ";
    cin >> eventId;
    cin.ignore(numeric_limits<streamsize>::max(), '\n');

    if (eventId == 0) {
        cout << "\nReturning to organizer menu...\n";
        return;
    }

    bool found = false;
    for (Event& event : events) {
        if (event.id == eventId && event.organizerId == organizer.id) {
            cout << "\nEvent: " << event.title << "\n";
            cout << "Current advertisement: " << (event.marketing.empty() ? "[None]" : event.marketing) << "\n";
            cout << "\nEnter new advertisement text (or leave blank to remove): ";

            string newAd;
            getline(cin, newAd);

            event.marketing = newAd;

            if (newAd.empty()) {
                cout << "\nAdvertisement removed for event: " << event.title << endl;
            }
            else {
                cout << "\nAdvertisement updated for event: " << event.title << endl;
                cout << "New advertisement: " << newAd << endl;
            }

            saveEventsToFile(events);
            found = true;
            break;
        }
    }

    if (!found) {
        cout << "\nInvalid event ID or you are not the organizer of this event.\n";
    }

    cout << "\nPress Enter to return...";
    cin.get();
}