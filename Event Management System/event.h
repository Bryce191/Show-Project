#pragma once
#include <string>
#include <vector>
#include <fstream>
#include <algorithm>
#include "user.h"

using namespace std;

enum class EventStatus {
    UPCOMING,      
    ONGOING,        
    COMPLETED,      
    CANCELLED       
};


struct Rating {
    int attendeeId;
    double rating;         
    string comment;
    string complaint;   
};


struct Event {
    int id = 0;
    string title;
    string description;
    string date;
    string time;
    string location;
    int organizerId = 0;
    vector<int> attendees;
    int expectedParticipants = 0;
    double totalFee = 0.0;
    double themeCost = 0.0;
    string themeName;
    string vendorName;
    string marketing;

    EventStatus status = EventStatus::UPCOMING;
    vector<Rating> ratings;
    double averageRating = 0.0;

    static const vector<string> slotOptions;
};


string statusToString(EventStatus status);
EventStatus stringToStatus(const string& str);

void saveEventsToFile(const vector<Event>& events, const string& filename = "events.dat");
void loadEventsFromFile(vector<Event>& events, const string& filename = "events.dat");
int generateEventId(const vector<Event>& events);

double calculateTotalFee(int venueCost, int participants, double themeCost);


