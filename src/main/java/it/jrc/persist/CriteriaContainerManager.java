package it.jrc.persist;


import it.jrc.auth.RoleManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.QueryModifierDelegate;
import com.vaadin.addon.jpacontainer.provider.MutableLocalEntityProvider;

/**
 * Manages container construction and entity persistence
 * 
 * @author Will Temperley
 * 
 */
public class CriteriaContainerManager<T> {

    private Dao dao;
    private JPAContainer<T> container;

    private Class<T> clazz;
    
    private boolean canDelete;
    private boolean canUpdate;
    private boolean canCreate;
    
    private Logger logger = LoggerFactory.getLogger(CriteriaContainerManager.class);

    public CriteriaContainerManager(Dao dao, Class<T> clazz, QueryModifierDelegate qmd) {

        this.dao = dao;
        this.clazz = clazz;

        container = new JPAContainer<T>(clazz);
        
        MutableLocalEntityProvider<T> entityProvider = new MutableLocalEntityProvider<T>(clazz);

        entityProvider.setQueryModifierDelegate(qmd);
        entityProvider.setEntityManagerProvider(dao);
        
        entityProvider.setEntitiesDetached(false);
        container.setEntityProvider(entityProvider);
        container.isAutoCommit();
        
        RoleManager roleManager = dao.getRoleManager();
        String target = roleManager.getUrlForClass(clazz);
        
        canCreate = roleManager.canCreate(target);
        canUpdate = roleManager.canUpdate(target);
        canDelete = roleManager.canDelete(target);

    }

    public JPAContainer<T> getContainer() {
        return container;
    }

    public T newEntity() {
        try {
            T entity =  clazz.newInstance();
            EntityItem<T> x = container.createEntityItem(entity);
            return x.getEntity();
            
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds the entity to the container and commits, returning the ID.
     * 
     * @param entity
     * @return
     */
    public Object addEntity(T entity) {
        Object id = container.addEntity(entity);
        return id;
    }

    public void deleteEntity(T entity) {
        /*
         * Delete happens through finding the id then calling remove on the
         * container
         * 
         * TODO: this should be part of the Vaadin library, surely? Difficult to
         * understand why they don't follow the EntityManager api and work-flow
         * more closely.
         */
        Object id = dao.getId(entity);
        container.removeItem(id);
        container.commit();
    }

    public T findEntity(Object val) {
        EntityItem<T> item = container.getItem(val);
        if (item != null) {
            return item.getEntity();
        }
        return null;
    }
    public Object getId(Object entity) {
        return dao.getId(entity);
    }

    public boolean canDelete() {
        return canDelete;
    }
    
    public boolean canUpdate() {
        return canUpdate;
    }
    
    public boolean canCreate() {
        return canCreate;
    }

    public void refresh() {
        container.refresh();
    }
}
