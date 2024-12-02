package com.itranswarp.exchange.quotation;

import com.itranswarp.exchange.model.quotation.*;
import com.itranswarp.exchange.support.AbstractDbService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Component
@Transactional
public class QuotationDbService extends AbstractDbService {
    public void saveBars(SecBarEntity sec, MinBarEntity min, HourBarEntity hour, DayBarEntity dat){
        if (sec != null) db.insertIgnore(sec);
        if (min != null) db.insertIgnore(min);
        if (hour != null) db.insertIgnore(hour);
        if (dat != null) db.insertIgnore(dat);
    }

    public void saveTicks(List<TickEntity> tick){db.insertIgnore(tick);}
}
