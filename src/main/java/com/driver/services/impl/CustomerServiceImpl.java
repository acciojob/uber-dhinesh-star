package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.findById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll();
		int driverId = Integer.MAX_VALUE;
		Driver pickUpDriver = null;
		for(Driver driver:driverList){
			Cab cab = driver.getCab();
			if(cab.getAvailable()==Boolean.TRUE){
				if(driverId>driver.getDriverId()){
					driverId = driver.getDriverId();
					pickUpDriver = driver;
				}
			}
		}
		if(pickUpDriver==null){
			throw new Exception("No driver is available");
		}
		TripBooking tripBooking = new TripBooking(fromLocation,toLocation,distanceInKm,TripStatus.CONFIRMED,(distanceInKm*pickUpDriver.getCab().getPerKmRate()));
		pickUpDriver.getTripBookingList().add(tripBooking);
		Customer customer = customerRepository2.findById(customerId).get();
		customer.getTripBookingList().add(tripBooking);
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(pickUpDriver);
		return tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBookingRepository2.save(tripBooking);
	}
}
