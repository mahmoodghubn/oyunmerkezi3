package com.example.oyunmerkezi3.utils

import java.text.SimpleDateFormat
import java.util.*


class CalendarUtil(private val before: Int?) {
    private val cal: Calendar = Calendar.getInstance()
    private fun getLastYear(): Date {
        return Date(cal.get(Calendar.YEAR)-1,1,1)

    }
    private fun getLastMonth():Date{
        return if (cal.get(Calendar.MONTH) ==0){
            Date(cal.get(Calendar.YEAR)-1,12,1)
        }else{
            Date(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),1)
        }

    }
    private fun getLastWeek():Date{
        return when {
            cal.get(Calendar.DAY_OF_MONTH)-cal.get(Calendar.DAY_OF_WEEK)-7>0 -> {
                Date(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH)-cal.get(Calendar.DAY_OF_WEEK)-7)
            }
            cal.get(Calendar.MONTH)!=0 -> {
                Date(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),25)
            }
            else -> {
                Date(cal.get(Calendar.YEAR)-1,12,25)
            }
        }
    }

    fun dateBefore():Date{
        return when(before){
            1-> getLastWeek()
            2-> getLastMonth()
            else -> getLastYear()
        }
    }
    fun getCurrentDate():Date{
        val c = Calendar.getInstance().time
        val df =  SimpleDateFormat ("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = df.format (c)
        return Date( Integer.parseInt(formattedDate.subSequence(6,10).toString()) ,
            Integer.parseInt(formattedDate.subSequence(3,5).toString()),
            Integer.parseInt(formattedDate.subSequence(0,2).toString()))
    }
}