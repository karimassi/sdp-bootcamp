package ch.epfl.balelecbud.utility.notifications.concertFlow.objects;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ch.epfl.balelecbud.model.Slot;

@Dao
public interface ConcertOfInterestDAO {
    @Query("SELECT * FROM Slot")
    List<Slot> getAllConcertOfInterestList();

    @Insert
    void insertConcert(Slot concert);

    @Delete
    void removeConcert(Slot concert);

    @Query("DELETE FROM Slot WHERE Slot.id = :id")
    void removeConcertById(int id);
}
