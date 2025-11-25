#pragma once
#include <string>

using namespace std;

bool ProcessPayment(double amount);

int selectPaymentMethod();

void generateReceipt(double amount, const string& method, bool success);
