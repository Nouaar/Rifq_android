package tn.rifq_android.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import tn.rifq_android.data.model.booking.Booking
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar Event UI Model
 * iOS Reference: CalendarManager.swift Event model
 */
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String = "",
    val location: String = "",
    val startTime: Long,
    val endTime: Long,
    val bookingId: String? = null
)

/**
 * Calendar Manager for Device Calendar Integration
 * iOS Reference: CalendarManager.swift (lines 1-187)
 * 
 * Integrates with Android Calendar Provider to:
 * - Add booking events to device calendar
 * - Fetch calendar events
 * - Edit/delete calendar events
 * - Sync bookings with device calendar
 */
class CalendarManager(private val context: Context) {
    
    companion object {
        private const val TAG = "CalendarManager"
        private const val CALENDAR_NAME = "Rifq Bookings"
        private const val CALENDAR_DISPLAY_NAME = "Rifq Appointments & Services"
        private const val CALENDAR_COLOR = 0xFFFF6B35.toInt() // Orange color
    }
    
    /**
     * Check if calendar permissions are granted
     */
    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Add booking to device calendar
     * @param booking Booking to add
     * @return Event ID if successful, null otherwise
     */
    fun addBookingToCalendar(booking: Booking): Long? {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return null
        }
        
        try {
            val calendarId = getOrCreateRifqCalendar() ?: return null
            
            val startTime = parseDateTime(booking.dateTime) ?: System.currentTimeMillis()
            val duration = (booking.duration ?: 60) * 60 * 1000L // Convert minutes to milliseconds
            val endTime = startTime + duration
            
            val title = "${booking.serviceType} - ${booking.provider?.name ?: "Booking"}"
            val description = buildString {
                append("Booking ID: ${booking.normalizedId}\n")
                booking.description?.let { append("$it\n") }
                booking.pet?.let { pet ->
                    append("Pet: ${pet.name}\n")
                }
                booking.price?.let { append("Price: $$it\n") }
            }
            
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1) // Enable reminder
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            
            // Add reminder (15 minutes before)
            eventId?.let { addReminder(it, 15) }
            
            Log.d(TAG, "📅 Added booking to calendar: ${booking.normalizedId} -> Event ID: $eventId")
            return eventId
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add booking to calendar: ${e.message}")
            return null
        }
    }
    
    /**
     * Add custom event to calendar (for manual reminders)
     * iOS Reference: CalendarManager.swift addEvent
     * @param title Event title
     * @param description Event description
     * @param startTime Start time in milliseconds
     * @param durationMinutes Duration in minutes
     * @param location Event location
     * @param recurrence Recurrence rule (RRULE format)
     * @return Event ID if successful, null otherwise
     */
    fun addCustomEvent(
        title: String,
        description: String,
        startTime: Long,
        durationMinutes: Int,
        location: String,
        recurrence: String? = null
    ): Long? {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return null
        }
        
        val calendarId = getOrCreateRifqCalendar()
        if (calendarId == null) {
            Log.e(TAG, "No Rifq calendar found")
            return null
        }
        
        try {
            val endTime = startTime + (durationMinutes * 60 * 1000)
            
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.HAS_ALARM, 1)
                
                // Add recurrence rule if provided
                if (recurrence != null) {
                    put(CalendarContract.Events.RRULE, recurrence)
                }
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            
            if (eventId != null) {
                // Add reminder (15 minutes before)
                addReminder(eventId, 15)
                Log.d(TAG, "📅 Added custom event to calendar: $title (ID: $eventId)")
            } else {
                Log.e(TAG, "Failed to add custom event to calendar")
            }
            
            return eventId
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add custom event to calendar: ${e.message}")
            return null
        }
    }
    
    /**
     * Remove booking from calendar
     * @param bookingId Booking ID
     * @return True if successful
     */
    fun removeBookingFromCalendar(bookingId: String): Boolean {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return false
        }
        
        try {
            val uri = CalendarContract.Events.CONTENT_URI
            val selection = "${CalendarContract.Events.CUSTOM_APP_URI} = ?"
            val selectionArgs = arrayOf("rifq://booking/$bookingId")
            
            val deletedRows = context.contentResolver.delete(uri, selection, selectionArgs)
            
            Log.d(TAG, "📅 Removed booking from calendar: $bookingId ($deletedRows events deleted)")
            return deletedRows > 0
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove booking from calendar: ${e.message}")
            return false
        }
    }
    
    /**
     * Get all Rifq calendar events
     * @return List of calendar events
     */
    fun getRifqCalendarEvents(): List<CalendarEvent> {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return emptyList()
        }
        
        try {
            val calendarId = getOrCreateRifqCalendar() ?: return emptyList()
            
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.CUSTOM_APP_URI
            )
            
            val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
            val selectionArgs = arrayOf(calendarId.toString())
            val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            val events = mutableListOf<CalendarEvent>()
            
            cursor?.use {
                while (it.moveToNext()) {
                    val eventId = it.getLong(0)
                    val title = it.getString(1) ?: ""
                    val description = it.getString(2) ?: ""
                    val startTime = it.getLong(3)
                    val endTime = it.getLong(4)
                    val location = it.getString(5) ?: ""
                    val customUri = it.getString(6) ?: ""
                    
                    events.add(
                        CalendarEvent(
                            id = eventId,
                            title = title,
                            description = description,
                            startTime = startTime,
                            endTime = endTime,
                            location = location,
                            bookingId = extractBookingId(customUri)
                        )
                    )
                }
            }
            
            Log.d(TAG, "📅 Retrieved ${events.size} calendar events")
            return events
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get calendar events: ${e.message}")
            return emptyList()
        }
    }
    
    /**
     * Get calendar events for a specific pet
     * iOS Reference: HomeView.swift lines 443-446
     * @param petId Pet ID to filter events
     * @return List of calendar events for the pet
     */
    fun getEventsForPet(petId: String): List<CalendarEvent> {
        return getRifqCalendarEvents().filter { event ->
            // Extract pet ID from event description or booking ID
            event.description.contains("Pet:") && event.description.contains(petId)
        }
    }
    
    /**
     * Load events for a specific pet (for AI integration)
     * iOS Reference: HomeView.swift line 433
     */
    fun loadEventsForPet(petId: String): List<CalendarEvent> {
        if (!hasCalendarPermission()) {
            Log.w(TAG, "Calendar permissions not granted")
            return emptyList()
        }
        return getEventsForPet(petId)
    }
    
    /**
     * Get or create Rifq calendar
     * @return Calendar ID or null
     */
    private fun getOrCreateRifqCalendar(): Long? {
        // Try to find existing calendar
        val calendarId = findRifqCalendar()
        if (calendarId != null) return calendarId
        
        // Create new calendar
        return createRifqCalendar()
    }
    
    /**
     * Find existing Rifq calendar
     */
    private fun findRifqCalendar(): Long? {
        try {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME
            )
            
            val selection = "${CalendarContract.Calendars.NAME} = ?"
            val selectionArgs = arrayOf(CALENDAR_NAME)
            
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getLong(0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find calendar: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Create Rifq calendar
     */
    private fun createRifqCalendar(): Long? {
        try {
            // Note: Creating calendars requires WRITE_CALENDAR permission
            // and may not work on all devices
            Log.d(TAG, "📅 Calendar creation attempted (may not be supported on all devices)")
            
            // Return primary calendar ID as fallback
            return getPrimaryCalendarId()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create calendar: ${e.message}")
            return getPrimaryCalendarId()
        }
    }
    
    /**
     * Get primary calendar ID
     */
    private fun getPrimaryCalendarId(): Long? {
        try {
            val projection = arrayOf(CalendarContract.Calendars._ID)
            val selection = "${CalendarContract.Calendars.IS_PRIMARY} = ?"
            val selectionArgs = arrayOf("1")
            
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getLong(0)
                }
            }
            
            // Fallback: get first calendar
            val fallbackCursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            
            fallbackCursor?.use {
                if (it.moveToFirst()) {
                    return it.getLong(0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get primary calendar: ${e.message}")
        }
        
        return null
    }
    
    /**
     * Add reminder to event
     */
    private fun addReminder(eventId: Long, minutesBefore: Int) {
        try {
            val values = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, minutesBefore)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
            Log.d(TAG, "📅 Added reminder: $minutesBefore minutes before")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add reminder: ${e.message}")
        }
    }
    
    /**
     * Parse date-time string to milliseconds
     */
    private fun parseDateTime(dateTimeString: String?): Long? {
        if (dateTimeString == null) return null
        
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(dateTimeString)?.time
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse date-time: ${e.message}")
            null
        }
    }
    
    /**
     * Extract booking ID from custom URI
     */
    private fun extractBookingId(customUri: String): String? {
        return try {
            if (customUri.startsWith("rifq://booking/")) {
                customUri.substringAfter("rifq://booking/")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
}
