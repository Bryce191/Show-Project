#include "payment.h"
#include "helpers.h"
#include <iostream>
using namespace std;

int selectPaymentMethod() {
    int method;

    while (true) {
        cout << "Select Payment Method:\n";
        cout << "1. Credit Card\n";
        cout << "2. Online Banking\n";
        cout << "3. E-Wallet (TNG)\n";
        cout << "Enter choice: ";
        cin >> method;

        if (cin.fail()) {
            cin.clear();
            cin.ignore(numeric_limits<streamsize>::max(), '\n');
            cout << "Invalid input. Please enter a number (1-3).\n\n";
            continue;
        }

        if (method >= 1 && method <= 3) {
            return method;
        }
        else {
            cout << "Invalid choice. Please choose again.\n\n";
        }
    }
}

void generateReceipt(double amount, const string& method, bool success) {
    cout << "\n\n========== RECEIPT ==========\n";
    cout << "Payment Method: " << method << "\n";
    cout << "Amount Paid: RM" << amount << "\n";
    cout << (success ? "Status: SUCCESS\n" : "Status: FAILED\n");
    cout << "=============================\n\n";
}

bool validatePIN(const string& pin, int method) {
    if (method == 1) { // Credit Card
        if (pin.size() == 12) return true;
        cout << "Error: Credit Card PIN must be exactly 12 digits.\n";
    }
    else if (method == 2) { // Online Banking
        if (pin.size() >= 12 && pin.size() <= 16) return true;
        cout << "Error: Online Banking PIN must be 12 - 16 digits.\n";
    }
    else if (method == 3) { // TNG
        if (pin.size() == 6) return true;
        cout << "Error: E-Wallet (TNG) PIN must be exactly 6 digits.\n";
    }
    return false;
}

bool ProcessPayment(double amount) {
    clearScreen();
    cout << "\n===== PAYMENT & CHECKOUT =====\n";
    cout << "Total Amount Due: RM" << amount << "\n";

    int method = selectPaymentMethod();
    if (method == -1) {  // User cancelled at selection
        cout << "Payment cancelled.\n";
        return false;
    }

    string pin;
    bool valid = false;

    // Keep asking for PIN until valid
    do {
        cout << "Enter PIN: ";
        cin >> pin;
        valid = validatePIN(pin, method);
        if (!valid) cout << "Please try again.\n\n";
    } while (!valid);

    cout << "Processing payment...\n";

    string methodName;
    switch (method) {
    case 1: methodName = "Credit Card"; break;
    case 2: methodName = "Online Banking"; break;
    case 3: methodName = "E-Wallet (TNG)"; break;
    default:
        cout << "Invalid payment method.\n";
        return false;
    }

    generateReceipt(amount, methodName, true); // assume success
    return true;
}