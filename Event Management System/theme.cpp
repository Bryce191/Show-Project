#include "theme.h"
#include "helpers.h"
#include <iostream>
#include <iomanip>
#include <limits>
#include <string>

using namespace std;

const int THEME_COUNT = 3;
const int ITEM_COUNT = 4;

string themes[THEME_COUNT] = { "Princess", "Superhero", "Retro" };
string items[ITEM_COUNT] = { "Balloons", "Lights", "Banners", "Cake" };

// 2D array of decoration costs [theme][item]
int costs[THEME_COUNT][ITEM_COUNT] = {
    {200, 15, 30, 100},
    {180, 25, 50, 80},
    {220, 10, 40, 150}
};

string vendors[THEME_COUNT] = {
    "FairyTale Decorators",
    "Heroic Events Co.",
    "RetroVibe Planners"
};

double themeMenu(string& themeName, string& vendorName) {
    clearScreen();
    cout << "\n===== THEME & DECORATION PLANNER =====\n";
    cout << "Available Themes & Costs:\n\n";

    for (int i = 0; i < THEME_COUNT; i++) {
        cout << i + 1 << ". " << themes[i]
            << " (Vendor: " << vendors[i] << ")\n";

        int themeTotal = 0;
        for (int j = 0; j < ITEM_COUNT; j++) {
            cout << "    - " << setw(10) << items[j]
                << ": RM" << costs[i][j] << endl;
            themeTotal += costs[i][j];
        }
        cout << "    ----------------------\n";
        cout << "    Total Theme Package Cost: RM" << themeTotal << "\n\n";
    }

    cout << THEME_COUNT + 1 << ". No decoration package\n";
    cout << "\nSelect a Theme (1-" << THEME_COUNT + 1 << "): ";

    int themeChoice = getIntInput(1, THEME_COUNT + 1);

    if (themeChoice == THEME_COUNT + 1) {
        cout << "\nReturning without selecting a theme...";
        themeName = "";
        vendorName = "";
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cin.get();

        return 0.0;
    }

    themeChoice--;

    themeName = themes[themeChoice];
    vendorName = vendors[themeChoice];

    cout << "\nYou selected: " << themeName << " Theme\n";
    cout << "Vendor: " << vendorName << "\n";

    double total = 0;
    cout << "Decorations included:\n";
    for (int j = 0; j < ITEM_COUNT; j++) {
        cout << setw(10) << items[j] << ": RM" << costs[themeChoice][j] << endl;
        total += costs[themeChoice][j];
    }

    cout << "----------------------\n";
    cout << "Total Theme Package Cost: RM" << total << "\n\n";

    cout << "\nPress Enter to continue...";
    cin.ignore(numeric_limits<streamsize>::max(), '\n');
    cin.get();

    return total;
}
