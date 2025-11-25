#include "user.h"
#include <fstream>
#include <sstream>
#include <iostream>

using namespace std;

extern vector<User> users;

void saveUsersToFile(const vector<User>& users) {
    ofstream outFile("users.dat", ios::trunc);
    if (!outFile) {
        cerr << "Error: Cannot open users.dat for writing!" << endl;
        return;
    }

    for (const User& user : users) {
        // Use pipe delimiter and handle special characters
        outFile << user.id << "|"
            << user.username << "|"
            << user.password << "|"
            << user.role << "|"
            << user.name << "|"
            << user.email << "\n";
    }

    outFile.close();
}
void loadUsersFromFile(vector<User>& users) {
    users.clear();
    ifstream inFile("users.dat");

    if (!inFile) {
        cout << "Note: users.dat not found. Creating default admin user." << endl;
        // Create default admin user
        User admin;
        admin.id = 1000;
        admin.username = "admin";
        admin.password = "admin123";
        admin.role = "admin";
        admin.name = "System Administrator";
        admin.email = "admin@events.com";
        users.push_back(admin);
        saveUsersToFile(users);
        return;
    }

    string line;
    int lineNumber = 0;

    while (getline(inFile, line)) {
        lineNumber++;
        if (line.empty()) continue;

        // Replace any commas with pipes for consistency (if old format)
        size_t commaPos = line.find(',');
        if (commaPos != string::npos) {
            replace(line.begin(), line.end(), ',', '|');
        }

        stringstream ss(line);
        vector<string> tokens;
        string token;

        while (getline(ss, token, '|')) {
            tokens.push_back(token);
        }

        if (tokens.size() != 6) {
            cerr << "Warning: Invalid user format on line " << lineNumber
                << ". Expected 6 fields, got " << tokens.size() << endl;
            cerr << "Line content: " << line << endl;
            continue;
        }

        try {
            User user;
            user.id = stoi(tokens[0]);
            user.username = tokens[1];
            user.password = tokens[2];
            user.role = tokens[3];
            user.name = tokens[4];
            user.email = tokens[5];

            users.push_back(user);
        }
        catch (const exception& e) {
            cerr << "Warning: Error parsing user on line " << lineNumber
                << ": " << e.what() << endl;
            continue;
        }
    }

    inFile.close();

    cout << "Loaded " << users.size() << " users:" << endl;
    for (const User& user : users) {
        cout << "ID: " << user.id << ", Name: '" << user.name
            << "', Username: '" << user.username << "', Role: " << user.role << endl;
    }

    // Create default admin if no users loaded
    if (users.empty()) {
        cout << "No valid users found. Creating default admin user." << endl;
        User admin;
        admin.id = 1000;
        admin.username = "admin";
        admin.password = "admin123";
        admin.role = "admin";
        admin.name = "System Administrator";
        admin.email = "admin@events.com";
        users.push_back(admin);
        saveUsersToFile(users);
    }
}
int generateUserId() {
    static int nextId = -1; 

    
    if (nextId == -1) {
        int maxId = 0;
        for (const User& user : users) {
            if (user.id > maxId) {
                maxId = user.id;
            }
        }
        nextId = maxId + 1;
    }

    return nextId++;
}

