package mrtranqui.fem.datastore;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment.Value;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.*;
import java.util.Map.Entry;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class DatastoreService<E> {
    private static final int COUNT_MANY_PAGE_SIZE = 100000;
    private final Class<E> c;
    private static Set<String> classes = new HashSet<String>();

    public DatastoreService(Class<E> c) {
        this.c = c;
        registerClass(c);
        if (SystemProperty.environment.value() != Value.Production) {
            ObjectifyService.begin();
        }
    }

    public static void registerClass(Class c) {
        String classId = c.getPackage() + c.getName();
        if (!classes.contains(classId)) {
            synchronized (classes) {
                if (!classes.contains(classId)) {
                    ObjectifyService.factory().register(c);
                    classes.add(classId);
                }
            }
        }
    }

    public void clearSessionCache() {
        ofy().clear();
    }

    public E save(E e) {
        ofy().save().entity(e).now();
        return e;
    }

    public void saveAll(Iterable<? extends E> list) {
        ofy().save().entities(list).now();
    }

    public E get(Long id) {
        return ofy().load().type(c).id(id).now();
    }

    public E get(String id) {
        return ofy().load().type(c).id(id).now();
    }
    

    public void delete(E e) {
        ofy().delete().entity(e).now();
    }

    public void deleteAll(Iterable<E> e) {
        ofy().delete().entities(e).now();
    }

    public Collection<E> getAll(List<String> ids) {
        return ofy().load().type(c).ids(ids).values();
    }

    public Set<Entry<String, E>> getAllBySet(List<String> ids) {
        return ofy().load().type(c).ids(ids).entrySet();
    }

    public List<E> getBy(String order, int limit) {
        return ofy().load().type(c).order(order).limit(limit).list();
    }

    public List<E> getBy(String byString, Object byObject) {
        return ofy().load().type(c).filter(byString, byObject).list();
    }

    public List<E> getBy(String byString, Object byObject, String order,
                         int limit) {
        return ofy().load().type(c).filter(byString, byObject).order(order)
                .limit(limit).list();
    }

    public List<E> getBy(String filterColumns[], Object filterValues[],
                         String order, int limit) {
        Query<E> query = ofy().load().type(c);
        for (int i = 0; i < filterColumns.length; i++) {
            query = query.filter(filterColumns[i], filterValues[i]);
        }
        return query.order(order).limit(limit).list();
    }

    public int count(String filterColumns[], Object filterValues[]) {
        Query<E> query = ofy().load().type(c);
        for (int i = 0; i < filterColumns.length; i++) {
            query = query.filter(filterColumns[i], filterValues[i]);
        }
        return query.count();
    }

    public int count(String byString, Object byObject) {
        return ofy().load().type(c).filter(byString, byObject).count();
    }

    public void deleteById(long id) {
        ofy().delete().type(c).id(id).now();
    }
    
    public void deleteByIdWithParent(String id,Object parentKeyOrEntity) {
        ofy().delete().type(c).parent(parentKeyOrEntity).id(id).now();
    }

    public void deleteById(String id) {
        ofy().delete().type(c).id(id).now();
    }

    public List<E> getBy(String[] filterColumns, Object[] filterValues,
                         int limit) {
        Query<E> query = ofy().load().type(c);
        for (int i = 0; i < filterColumns.length; i++) {
            query = query.filter(filterColumns[i], filterValues[i]);
        }
        return query.limit(limit).list();
    }

    public E getWithoutCache(String id) {
        return ofy().cache(false).load().type(c).id(id).now();
    }

    public int countMany(String byString, Object byObject) {
        return countMany(new String[]{byString}, new Object[]{byObject});
    }

    public int countMany(String[] byStrings, Object[] byObject) {
        int total = 0;
        Cursor cursor = null;
        boolean carryOnToNextCursor = false;
        do {
            carryOnToNextCursor = false;
            Query<E> query = ofy().load().type(c).limit(COUNT_MANY_PAGE_SIZE);
            for (int i = 0; i < byStrings.length; i++) {
                query = query.filter(byStrings[i], byObject[i]);
            }
            if (cursor != null) {
                query = query.startAt(cursor);
            }

            QueryResultIterator<Key<E>> iterator = query.keys().iterator();
            while (iterator.hasNext()) {
                iterator.next();
                total++;
                carryOnToNextCursor = true;
            }
            cursor = iterator.getCursor();
        } while (carryOnToNextCursor);
        return total;
    }

    public QueryResultIterator<E> iterate(String[] columns, Object[] values, int limit, Cursor cursor) {
        Query<E> query = ofy().load().type(c).limit(limit);
        if (cursor != null) {
            query = query.startAt(cursor);
        }
        for (int i = 0; i < columns.length; i++) {
            query = query.filter(columns[i], values[i]);
        }
        return query.iterator();
    }

    public List<E> list() {
        Query<E> query = ofy().load().type(c);
        return query.list();
    }

    public QueryResultIterator<E> iterate(Cursor cursor) {
        Query<E> query = ofy().load().type(c);
        if(cursor != null){
            query = query.startAt(cursor);
        }
        return query.iterator();
    }

    public List<E> list(String[] columns, Object[] values) {
        Query<E> query = ofy().load().type(c);
        for (int i = 0; i < columns.length; i++) {
            query = query.filter(columns[i], values[i]);
        }
        return query.list();
    }

    public QueryResultIterator<E> pagedList(String[] columns, Object[] values, String orderBy) {
        Query<E> query = ofy().load().type(c);
        for (int i = 0; i < columns.length; i++) {
            query = query.filter(columns[i], values[i]);
        }
        if(orderBy != null)
            query = query.order(orderBy);
        return query.iterator();
    }
    
    public List<E> listForParent(Object parentKeyOrEntity,String[] columns, Object[] values) {
        Query<E> query = ofy().load().type(c).ancestor(parentKeyOrEntity);
        for (int i = 0; i < columns.length; i++) {
            query = query.filter(columns[i], values[i]);
        }
        return query.list();
    }

    public List<Key<E>> listKeysForParent(Object parentKeyOrEntity) {
        return ofy().load().type(c).ancestor(parentKeyOrEntity).keys().list();
    }

    public void deleteKeysAsynchronously(List<Key<E>> keys) {
        ofy().delete().keys(keys);
    }


    public QueryResultIterator<E> listEntitiesForParent(Object parentKeyOrEntity, String[] columns, Object[] values, int chunkSize) {
        Query<E> query = ofy().load().type(c).ancestor(parentKeyOrEntity).chunk(chunkSize);
        for (int i = 0; i < columns.length; i++) {
            query = query.filter(columns[i], values[i]);
        }
        return query.iterator();
    }

    public E get(Key parent, String id) {
        return ofy().load().type(c).parent(parent).id(id).now();
    }

    public static Map getByKeys(Key... keys) {
        return ofy().load().keys(keys);
    }
}
