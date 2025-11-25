#include "helpers.h"
#include <iostream>
#include <ctime>
#include <limits>
#include <cstdlib> 
#include <cctype>

using namespace std;

void displayIntro() {
    clearScreen();
    cout << "============================================\n";
    cout << "       EVENT MANAGEMENT SYSTEM\n";
    cout << "============================================\n";
    cout << "  A comprehensive solution for organizing\n";
    cout << "  and managing personal events and parties\n";
    cout << "============================================\n\n";
}

void clearScreen() {
#ifdef _WIN32
    system("cls");
#else
    system("clear");
#endif
}

void debugPrintFileContents(const string& filename) {
    ifstream file(filename);
    if (!file) {
        cout << "Debug: Cannot open " << filename << endl;
        return;
    }

    cout << "Debug: Contents of " << filename << ":" << endl;
    string line;
    int lineNum = 1;
    while (getline(file, line)) {
        cout << "Line " << lineNum << ": [" << line << "]" << endl;
        lineNum++;
    }
    cout << "Debug: End of file" << endl;
    file.close();
}

bool isValidEmail(const string& email) {
    size_t atPos = email.find('@');
    size_t dotPos = email.rfind('.');

    
    bool validFormat = (atPos != string::npos &&
        dotPos != string::npos &&
        dotPos > atPos + 1 &&
        dotPos < email.length() - 1);

    if (!validFormat) return false;

    
    for (char c : email) {
        if (isalpha(c) && !islower(c)) {
            return false;  
        }
    }

    return true;
}


void pauseScreen() {
    cout << "\nPress Enter to continue...";
    cin.ignore(numeric_limits<streamsize>::max(), '\n');
    cin.get();
}

char getYesNoInput() {
    char c;
    while (true) {
        cin >> c;
        cin.ignore(numeric_limits<streamsize>::max(), '\n');

        if (c == 'y' || c == 'Y' || c == 'n' || c == 'N') {
            return c;
        }

        cout << "Invalid input. Please enter 'y' or 'n': ";
        cin.clear();
    }
}




bool isValidDate(const string& date) {
    if (date.length() != 10) return false;
    if (date[4] != '-' || date[7] != '-') return false;

    try {
        int year = stoi(date.substr(0, 4));
        int month = stoi(date.substr(5, 2));
        int day = stoi(date.substr(8, 2));

        if (year < 2025 || year > 2028) return false;
        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31) return false;
    }
    catch (...) {
        return false;
    }

    return true;
}

string getPasswordInput(const string& prompt) {
    string password;
    bool validPassword = false;

    while (!validPassword) {
        cout << prompt;
        cin >> password;

        if (password == "0") {
            return "0"; // Cancellation
        }

        if (password.empty()) {
            cout << "? Password cannot be empty. Please try again.\n";
        }
        else if (password.length() < 4) {
            cout << "? Password must be at least 4 characters long.\n";
        }
        else {
            validPassword = true;
        }
    }

    return password;
}

int getIntInput(int min, int max, bool allowZeroExit) {
    int value;
    while (true) {
        cin >> value;
        if (cin.fail()) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input. Please enter a number";
            if (allowZeroExit) cout << " (0 to exit)";
            cout << ": ";
        }
        else if (allowZeroExit && value == 0) {
            return 0; // Special value indicating exit
        }
        else if (value < min || value > max) {
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input. Please enter a number between " << min << " and " << max;
            if (allowZeroExit) cout << " (0 to exit)";
            cout << ": ";
        }
        else {
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            return value;
        }
    }
}
bool isValidTime(const string& time) {
    if (time.length() != 5) return false;
    if (time[2] != ':') return false;

    try {
        int hour = stoi(time.substr(0, 2));
        int minute = stoi(time.substr(3, 2));

        if (hour < 0 || hour > 23) return false;
        if (minute < 0 || minute > 59) return false;
    }
    catch (...) {
        return false;
    }

    return true;
}
