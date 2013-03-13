package org.apache.marmotta.ldcache.sail;

import info.aduna.iteration.CloseableIteration;

import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.UnionIteration;
import org.apache.marmotta.commons.sesame.filter.AlwaysTrueFilter;
import org.apache.marmotta.commons.sesame.filter.SesameFilter;
import org.apache.marmotta.commons.sesame.repository.ResourceUtils;
import org.apache.marmotta.ldcache.api.LDCachingConnection;
import org.apache.marmotta.ldcache.services.LDCache;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A sail connection wrapper that queries both the wrapped connection and the cached triples provided by an
 * LDCache instance.
 *
 */
public class GenericCachingSailConnection extends NotifyingSailConnectionWrapper {

    private static Logger log = LoggerFactory.getLogger(GenericCachingSailConnection.class);

    private final LDCache ldcache;
    private final SesameFilter<Resource> acceptForCaching;

    public GenericCachingSailConnection(NotifyingSailConnection connection, LDCache ldcache) {
        this(connection, ldcache, new AlwaysTrueFilter<Resource>());
    }


    public GenericCachingSailConnection(NotifyingSailConnection connection, LDCache ldcache, SesameFilter<Resource> acceptForCaching) {
        super(connection);
        this.ldcache = ldcache;
        this.acceptForCaching = acceptForCaching;
    }

    /**
     * Get the statements for the query pattern passed as arguments. In case the subject resource is acceptable for caching,
     * refreshes the cache for this resource and returns a merged query result together with the wrapped connection.
     * Otherwise, simply delegates to the wrapped connection.
     *
     * @param subj
     * @param pred
     * @param obj
     * @param includeInferred
     * @param contexts
     * @return
     * @throws SailException
     */
    @Override
    public CloseableIteration<? extends Statement, SailException> getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts) throws SailException {

        if (accept(subj)) {
            log.debug("Refreshing resource: {}", subj.stringValue());
            ldcache.refreshResource((URI) subj, false);

            try {
                LDCachingConnection cachingConnection = ldcache.getCacheConnection(subj.stringValue());
                try {
                    // join the results of the cache connection and the wrapped connection in a single result
                    return new UnionIteration<Statement, SailException>(
                            new ExceptionConvertingIteration<Statement, SailException>(cachingConnection.getStatements(subj,pred,obj,includeInferred, contexts)) {
                                @Override
                                protected SailException convert(Exception e) {
                                    return new SailException("error while accessing cache connection",e);
                                }
                            },
                            super.getStatements(subj, pred, obj, includeInferred, contexts)
                    );
                } finally {
                    cachingConnection.close();
                }
            } catch (RepositoryException e) {
                throw new SailException("error while accessing cache connection",e);
            }
        } else {
            return super.getStatements(subj, pred, obj, includeInferred, contexts);
        }
    }


    private boolean accept(Resource subj) {
        return subj != null && ResourceUtils.isURI(subj) && acceptForCaching.accept(subj);
    }

}
