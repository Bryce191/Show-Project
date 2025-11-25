#pragma once
#include <iostream>
#include <string>
#include <iomanip>
#include <limits>
#include <ctime>
#include <conio.h> 
#include "user.h"
#include "event.h"

using namespace std;

extern vector<User> users;
extern vector<Event> events;

void displayIntro();
void clearScreen();
bool isValidEmail(const string& email);
int getIntInput(int min, int max, bool allowZeroExit = false);
bool isValidDate(const string& date);
bool isValidTime(const string& time);
void debugPrintFileContents(const string& filename);
void pauseScreen();
char getYesNoInput();
string statusToString(EventStatus status);
EventStatus stringToStatus(const string& str);

string getPasswordInput(const string& prompt = "Password: ");