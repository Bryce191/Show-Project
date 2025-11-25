#pragma once
#include <string>
#include <vector>
#include <fstream>
#include <algorithm>

using namespace std;

struct User {
    int id = 0;
    string username;
    string password;
    string role; 
    string name;
    string email;
};

void saveUsersToFile(const vector<User>& users);
void loadUsersFromFile(vector<User>& users);
int generateUserId();
