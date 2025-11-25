#ifndef MARKETING_H
#define MARKETING_H

#include <vector>
#include "event.h"
#include "user.h"    

using namespace std;  
// Show the current top event
void displayTopEvent(const std::vector<Event>& events);

// Create advertisement for an event
void createAdvertisement(User& organizer, std::vector<Event>& events);

#endif