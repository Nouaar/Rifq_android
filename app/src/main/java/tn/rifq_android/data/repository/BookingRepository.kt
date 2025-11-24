package tn.rifq_android.data.repository

import tn.rifq_android.data.api.BookingApi
import tn.rifq_android.data.model.booking.Booking
import tn.rifq_android.data.model.booking.CreateBookingRequest
import tn.rifq_android.data.model.booking.UpdateBookingRequest

class BookingRepository(private val api: BookingApi) {
    suspend fun getBookings(role: String? = null, status: String? = null): List<Booking> {
        return api.getBookings(role, status)
    }

    suspend fun getBookingById(bookingId: String): Booking {
        return api.getBookingById(bookingId)
    }

    suspend fun createBooking(request: CreateBookingRequest): Booking {
        return api.createBooking(request)
    }

    suspend fun updateBooking(bookingId: String, request: UpdateBookingRequest): Booking {
        return api.updateBooking(bookingId, request)
    }

    suspend fun deleteBooking(bookingId: String) {
        api.deleteBooking(bookingId)
    }
}
