package by.kokhan;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TorrentSerializer {
    private static final Gson gson = new Gson();

    public static void serialize(Iterable<TorrentInfo> torrentInfoList){
        try (FileWriter writer = new FileWriter("data.json")) {
            var serialisedDataList = new LinkedList<SerialisedData>();
            for (var torrentInfo : torrentInfoList)
                serialisedDataList.add(new SerialisedData(torrentInfo));
            gson.toJson(serialisedDataList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<SerialisedData> deserialize(){
        try (FileReader reader = new FileReader("data.json")) {
            List<SerialisedData> serialisedDataList = gson.fromJson(reader, new TypeToken<List<SerialisedData>>(){}.getType());
            return serialisedDataList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
