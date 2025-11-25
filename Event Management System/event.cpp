#include "event.h"
#include <sstream>
#include <iostream>
#include <climits>

using namespace std;

int generateEventId(const vector<Event>& events) {
    int maxId = 0;
    for (const auto& e : events) {
        if (e.id > maxId) maxId = e.id;
    }
    return maxId + 1; 
}

string statusToString(EventStatus status) {
    switch (status) {
    case EventStatus::UPCOMING: return "UPCOMING";
    case EventStatus::ONGOING: return "ONGOING";
    case EventStatus::COMPLETED: return "COMPLETED";
    case EventStatus::CANCELLED: return "CANCELLED";
    default: return "UPCOMING";
    }
}

EventStatus stringToStatus(const string& str) {
    if (str == "UPCOMING") return EventStatus::UPCOMING;
    if (str == "ONGOING") return EventStatus::ONGOING;
    if (str == "COMPLETED") return EventStatus::COMPLETED;
    if (str == "CANCELLED") return EventStatus::CANCELLED;
    return EventStatus::UPCOMING;
}

void saveEventsToFile(const vector<Event>& events, const string& filename) {
    ofstream outFile(filename, ios::trunc);
    if (!outFile) {
        cerr << "Error: Cannot open " << filename << " for writing!" << endl;
        return;
    }

    for (const auto& ev : events) {
        outFile << ev.id << '|'
            << ev.title << '|'
            << ev.description << '|'
            << ev.date << '|'
            << ev.time << '|'
            << ev.location << '|'
            << ev.organizerId << '|';

        // Save attendees as comma-separated values
        for (size_t i = 0; i < ev.attendees.size(); i++) {
            outFile << ev.attendees[i];
            if (i < ev.attendees.size() - 1) outFile << ",";
        }
        outFile << '|';

        outFile << ev.expectedParticipants << '|'
            << ev.totalFee << '|'
            << ev.themeCost << '|'
            << ev.themeName << '|'
            << ev.vendorName << '|'
            << ev.marketing << '|'
            << statusToString(ev.status) << '|'
            << ev.averageRating << '|';

        // Save ratings
        for (size_t i = 0; i < ev.ratings.size(); i++) {
            outFile << ev.ratings[i].attendeeId << ','
                << ev.ratings[i].rating << ','
                << ev.ratings[i].comment << ','
                << ev.ratings[i].complaint;
            if (i < ev.ratings.size() - 1) outFile << ';';
        }
        outFile << '\n';
    }
}

void loadEventsFromFile(vector<Event>& events, const string& filename) {
    events.clear();
    ifstream inFile(filename);

    if (!inFile) {
        cout << "Note: " << filename << " not found. Starting with empty events list." << endl;
        return;
    }

    string line;
    int lineNumber = 0;

    while (getline(inFile, line)) {
        lineNumber++;
        if (line.empty()) continue;

        stringstream ss(line);
        string token;
        vector<string> tokens;

        while (getline(ss, token, '|')) {
            tokens.push_back(token);
        }

        if (tokens.size() < 16) {
            cerr << "Warning: Invalid format on line " << lineNumber << endl;
            continue;
        }

        try {
            Event ev;
            ev.id = stoi(tokens[0]);
            ev.title = tokens[1];
            ev.description = tokens[2];
            ev.date = tokens[3];
            ev.time = tokens[4];
            ev.location = tokens[5];
            ev.organizerId = stoi(tokens[6]);

            // Parse attendees
            if (!tokens[7].empty()) {
                stringstream attStream(tokens[7]);
                string attId;
                while (getline(attStream, attId, ',')) {
                    if (!attId.empty()) ev.attendees.push_back(stoi(attId));
                }
            }

            ev.expectedParticipants = stoi(tokens[8]);
            ev.totalFee = stod(tokens[9]);
            ev.themeCost = stod(tokens[10]);
            ev.themeName = tokens[11];
            ev.vendorName = tokens[12];
            ev.marketing = tokens[13];
            ev.status = stringToStatus(tokens[14]);
            ev.averageRating = stod(tokens[15]);

            // Parse ratings
            if (tokens.size() > 16 && !tokens[16].empty()) {
                stringstream ratingStream(tokens[16]);
                string ratingEntry;
                while (getline(ratingStream, ratingEntry, ';')) {
                    stringstream entryStream(ratingEntry);
                    string part;
                    Rating rating;

                    getline(entryStream, part, ',');
                    rating.attendeeId = stoi(part);

                    getline(entryStream, part, ',');
                    rating.rating = stod(part);

                    getline(entryStream, rating.comment, ',');
                    getline(entryStream, rating.complaint, ',');

                    ev.ratings.push_back(rating);
                }
            }

            events.push_back(ev);
        }
        catch (const exception& e) {
            cerr << "Warning: Error parsing line " << lineNumber << ": " << e.what() << endl;
        }
    }
}


const vector<string> Event::slotOptions = {
    "09:00-12:00",
    "12:00-15:00",
    "15:00-18:00",
    "18:00-21:00"
};

double calculateTotalFee(int venueCost, int participants, double themeCost) {
    return venueCost + (participants * 5) + themeCost;
}
