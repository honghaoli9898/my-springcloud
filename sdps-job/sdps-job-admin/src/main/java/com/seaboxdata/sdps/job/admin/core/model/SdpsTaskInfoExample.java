package com.seaboxdata.sdps.job.admin.core.model;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SdpsTaskInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SdpsTaskInfoExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andSubmitIdIsNull() {
            addCriterion("submit_id is null");
            return (Criteria) this;
        }

        public Criteria andSubmitIdIsNotNull() {
            addCriterion("submit_id is not null");
            return (Criteria) this;
        }

        public Criteria andSubmitIdEqualTo(Long value) {
            addCriterion("submit_id =", value, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdNotEqualTo(Long value) {
            addCriterion("submit_id <>", value, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdGreaterThan(Long value) {
            addCriterion("submit_id >", value, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdGreaterThanOrEqualTo(Long value) {
            addCriterion("submit_id >=", value, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdLessThan(Long value) {
            addCriterion("submit_id <", value, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdLessThanOrEqualTo(Long value) {
            addCriterion("submit_id <=", value, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdIn(List<Long> values) {
            addCriterion("submit_id in", values, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdNotIn(List<Long> values) {
            addCriterion("submit_id not in", values, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdBetween(Long value1, Long value2) {
            addCriterion("submit_id between", value1, value2, "submitId");
            return (Criteria) this;
        }

        public Criteria andSubmitIdNotBetween(Long value1, Long value2) {
            addCriterion("submit_id not between", value1, value2, "submitId");
            return (Criteria) this;
        }

        public Criteria andClusterIdIsNull() {
            addCriterion("cluster_id is null");
            return (Criteria) this;
        }

        public Criteria andClusterIdIsNotNull() {
            addCriterion("cluster_id is not null");
            return (Criteria) this;
        }

        public Criteria andClusterIdEqualTo(Integer value) {
            addCriterion("cluster_id =", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotEqualTo(Integer value) {
            addCriterion("cluster_id <>", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdGreaterThan(Integer value) {
            addCriterion("cluster_id >", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("cluster_id >=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThan(Integer value) {
            addCriterion("cluster_id <", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdLessThanOrEqualTo(Integer value) {
            addCriterion("cluster_id <=", value, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdIn(List<Integer> values) {
            addCriterion("cluster_id in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotIn(List<Integer> values) {
            addCriterion("cluster_id not in", values, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdBetween(Integer value1, Integer value2) {
            addCriterion("cluster_id between", value1, value2, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterIdNotBetween(Integer value1, Integer value2) {
            addCriterion("cluster_id not between", value1, value2, "clusterId");
            return (Criteria) this;
        }

        public Criteria andClusterNameIsNull() {
            addCriterion("cluster_name is null");
            return (Criteria) this;
        }

        public Criteria andClusterNameIsNotNull() {
            addCriterion("cluster_name is not null");
            return (Criteria) this;
        }

        public Criteria andClusterNameEqualTo(String value) {
            addCriterion("cluster_name =", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotEqualTo(String value) {
            addCriterion("cluster_name <>", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameGreaterThan(String value) {
            addCriterion("cluster_name >", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameGreaterThanOrEqualTo(String value) {
            addCriterion("cluster_name >=", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLessThan(String value) {
            addCriterion("cluster_name <", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLessThanOrEqualTo(String value) {
            addCriterion("cluster_name <=", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLike(String value) {
            addCriterion("cluster_name like", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotLike(String value) {
            addCriterion("cluster_name not like", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameIn(List<String> values) {
            addCriterion("cluster_name in", values, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotIn(List<String> values) {
            addCriterion("cluster_name not in", values, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameBetween(String value1, String value2) {
            addCriterion("cluster_name between", value1, value2, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotBetween(String value1, String value2) {
            addCriterion("cluster_name not between", value1, value2, "clusterName");
            return (Criteria) this;
        }

        public Criteria andUserNameIsNull() {
            addCriterion("user_name is null");
            return (Criteria) this;
        }

        public Criteria andUserNameIsNotNull() {
            addCriterion("user_name is not null");
            return (Criteria) this;
        }

        public Criteria andUserNameEqualTo(String value) {
            addCriterion("user_name =", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotEqualTo(String value) {
            addCriterion("user_name <>", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameGreaterThan(String value) {
            addCriterion("user_name >", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("user_name >=", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameLessThan(String value) {
            addCriterion("user_name <", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameLessThanOrEqualTo(String value) {
            addCriterion("user_name <=", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameLike(String value) {
            addCriterion("user_name like", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotLike(String value) {
            addCriterion("user_name not like", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameIn(List<String> values) {
            addCriterion("user_name in", values, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotIn(List<String> values) {
            addCriterion("user_name not in", values, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameBetween(String value1, String value2) {
            addCriterion("user_name between", value1, value2, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotBetween(String value1, String value2) {
            addCriterion("user_name not between", value1, value2, "userName");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdIsNull() {
            addCriterion("yarn_app_id is null");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdIsNotNull() {
            addCriterion("yarn_app_id is not null");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdEqualTo(String value) {
            addCriterion("yarn_app_id =", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdNotEqualTo(String value) {
            addCriterion("yarn_app_id <>", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdGreaterThan(String value) {
            addCriterion("yarn_app_id >", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdGreaterThanOrEqualTo(String value) {
            addCriterion("yarn_app_id >=", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdLessThan(String value) {
            addCriterion("yarn_app_id <", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdLessThanOrEqualTo(String value) {
            addCriterion("yarn_app_id <=", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdLike(String value) {
            addCriterion("yarn_app_id like", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdNotLike(String value) {
            addCriterion("yarn_app_id not like", value, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdIn(List<String> values) {
            addCriterion("yarn_app_id in", values, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdNotIn(List<String> values) {
            addCriterion("yarn_app_id not in", values, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdBetween(String value1, String value2) {
            addCriterion("yarn_app_id between", value1, value2, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppIdNotBetween(String value1, String value2) {
            addCriterion("yarn_app_id not between", value1, value2, "yarnAppId");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameIsNull() {
            addCriterion("yarn_app_name is null");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameIsNotNull() {
            addCriterion("yarn_app_name is not null");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameEqualTo(String value) {
            addCriterion("yarn_app_name =", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameNotEqualTo(String value) {
            addCriterion("yarn_app_name <>", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameGreaterThan(String value) {
            addCriterion("yarn_app_name >", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameGreaterThanOrEqualTo(String value) {
            addCriterion("yarn_app_name >=", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameLessThan(String value) {
            addCriterion("yarn_app_name <", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameLessThanOrEqualTo(String value) {
            addCriterion("yarn_app_name <=", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameLike(String value) {
            addCriterion("yarn_app_name like", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameNotLike(String value) {
            addCriterion("yarn_app_name not like", value, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameIn(List<String> values) {
            addCriterion("yarn_app_name in", values, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameNotIn(List<String> values) {
            addCriterion("yarn_app_name not in", values, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameBetween(String value1, String value2) {
            addCriterion("yarn_app_name between", value1, value2, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnAppNameNotBetween(String value1, String value2) {
            addCriterion("yarn_app_name not between", value1, value2, "yarnAppName");
            return (Criteria) this;
        }

        public Criteria andYarnQueueIsNull() {
            addCriterion("yarn_queue is null");
            return (Criteria) this;
        }

        public Criteria andYarnQueueIsNotNull() {
            addCriterion("yarn_queue is not null");
            return (Criteria) this;
        }

        public Criteria andYarnQueueEqualTo(String value) {
            addCriterion("yarn_queue =", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueNotEqualTo(String value) {
            addCriterion("yarn_queue <>", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueGreaterThan(String value) {
            addCriterion("yarn_queue >", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueGreaterThanOrEqualTo(String value) {
            addCriterion("yarn_queue >=", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueLessThan(String value) {
            addCriterion("yarn_queue <", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueLessThanOrEqualTo(String value) {
            addCriterion("yarn_queue <=", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueLike(String value) {
            addCriterion("yarn_queue like", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueNotLike(String value) {
            addCriterion("yarn_queue not like", value, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueIn(List<String> values) {
            addCriterion("yarn_queue in", values, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueNotIn(List<String> values) {
            addCriterion("yarn_queue not in", values, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueBetween(String value1, String value2) {
            addCriterion("yarn_queue between", value1, value2, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnQueueNotBetween(String value1, String value2) {
            addCriterion("yarn_queue not between", value1, value2, "yarnQueue");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlIsNull() {
            addCriterion("yarn_tracking_url is null");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlIsNotNull() {
            addCriterion("yarn_tracking_url is not null");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlEqualTo(String value) {
            addCriterion("yarn_tracking_url =", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlNotEqualTo(String value) {
            addCriterion("yarn_tracking_url <>", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlGreaterThan(String value) {
            addCriterion("yarn_tracking_url >", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlGreaterThanOrEqualTo(String value) {
            addCriterion("yarn_tracking_url >=", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlLessThan(String value) {
            addCriterion("yarn_tracking_url <", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlLessThanOrEqualTo(String value) {
            addCriterion("yarn_tracking_url <=", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlLike(String value) {
            addCriterion("yarn_tracking_url like", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlNotLike(String value) {
            addCriterion("yarn_tracking_url not like", value, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlIn(List<String> values) {
            addCriterion("yarn_tracking_url in", values, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlNotIn(List<String> values) {
            addCriterion("yarn_tracking_url not in", values, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlBetween(String value1, String value2) {
            addCriterion("yarn_tracking_url between", value1, value2, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andYarnTrackingUrlNotBetween(String value1, String value2) {
            addCriterion("yarn_tracking_url not between", value1, value2, "yarnTrackingUrl");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeIsNull() {
            addCriterion("application_type is null");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeIsNotNull() {
            addCriterion("application_type is not null");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeEqualTo(String value) {
            addCriterion("application_type =", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeNotEqualTo(String value) {
            addCriterion("application_type <>", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeGreaterThan(String value) {
            addCriterion("application_type >", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeGreaterThanOrEqualTo(String value) {
            addCriterion("application_type >=", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeLessThan(String value) {
            addCriterion("application_type <", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeLessThanOrEqualTo(String value) {
            addCriterion("application_type <=", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeLike(String value) {
            addCriterion("application_type like", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeNotLike(String value) {
            addCriterion("application_type not like", value, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeIn(List<String> values) {
            addCriterion("application_type in", values, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeNotIn(List<String> values) {
            addCriterion("application_type not in", values, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeBetween(String value1, String value2) {
            addCriterion("application_type between", value1, value2, "applicationType");
            return (Criteria) this;
        }

        public Criteria andApplicationTypeNotBetween(String value1, String value2) {
            addCriterion("application_type not between", value1, value2, "applicationType");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdIsNull() {
            addCriterion("xxl_log_id is null");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdIsNotNull() {
            addCriterion("xxl_log_id is not null");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdEqualTo(Long value) {
            addCriterion("xxl_log_id =", value, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdNotEqualTo(Long value) {
            addCriterion("xxl_log_id <>", value, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdGreaterThan(Long value) {
            addCriterion("xxl_log_id >", value, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdGreaterThanOrEqualTo(Long value) {
            addCriterion("xxl_log_id >=", value, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdLessThan(Long value) {
            addCriterion("xxl_log_id <", value, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdLessThanOrEqualTo(Long value) {
            addCriterion("xxl_log_id <=", value, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdIn(List<Long> values) {
            addCriterion("xxl_log_id in", values, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdNotIn(List<Long> values) {
            addCriterion("xxl_log_id not in", values, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdBetween(Long value1, Long value2) {
            addCriterion("xxl_log_id between", value1, value2, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlLogIdNotBetween(Long value1, Long value2) {
            addCriterion("xxl_log_id not between", value1, value2, "xxlLogId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdIsNull() {
            addCriterion("xxl_job_id is null");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdIsNotNull() {
            addCriterion("xxl_job_id is not null");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdEqualTo(Long value) {
            addCriterion("xxl_job_id =", value, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdNotEqualTo(Long value) {
            addCriterion("xxl_job_id <>", value, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdGreaterThan(Long value) {
            addCriterion("xxl_job_id >", value, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdGreaterThanOrEqualTo(Long value) {
            addCriterion("xxl_job_id >=", value, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdLessThan(Long value) {
            addCriterion("xxl_job_id <", value, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdLessThanOrEqualTo(Long value) {
            addCriterion("xxl_job_id <=", value, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdIn(List<Long> values) {
            addCriterion("xxl_job_id in", values, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdNotIn(List<Long> values) {
            addCriterion("xxl_job_id not in", values, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdBetween(Long value1, Long value2) {
            addCriterion("xxl_job_id between", value1, value2, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andXxlJobIdNotBetween(Long value1, Long value2) {
            addCriterion("xxl_job_id not between", value1, value2, "xxlJobId");
            return (Criteria) this;
        }

        public Criteria andShellPathIsNull() {
            addCriterion("shell_path is null");
            return (Criteria) this;
        }

        public Criteria andShellPathIsNotNull() {
            addCriterion("shell_path is not null");
            return (Criteria) this;
        }

        public Criteria andShellPathEqualTo(String value) {
            addCriterion("shell_path =", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathNotEqualTo(String value) {
            addCriterion("shell_path <>", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathGreaterThan(String value) {
            addCriterion("shell_path >", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathGreaterThanOrEqualTo(String value) {
            addCriterion("shell_path >=", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathLessThan(String value) {
            addCriterion("shell_path <", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathLessThanOrEqualTo(String value) {
            addCriterion("shell_path <=", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathLike(String value) {
            addCriterion("shell_path like", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathNotLike(String value) {
            addCriterion("shell_path not like", value, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathIn(List<String> values) {
            addCriterion("shell_path in", values, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathNotIn(List<String> values) {
            addCriterion("shell_path not in", values, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathBetween(String value1, String value2) {
            addCriterion("shell_path between", value1, value2, "shellPath");
            return (Criteria) this;
        }

        public Criteria andShellPathNotBetween(String value1, String value2) {
            addCriterion("shell_path not between", value1, value2, "shellPath");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andExt0IsNull() {
            addCriterion("ext_0 is null");
            return (Criteria) this;
        }

        public Criteria andExt0IsNotNull() {
            addCriterion("ext_0 is not null");
            return (Criteria) this;
        }

        public Criteria andExt0EqualTo(String value) {
            addCriterion("ext_0 =", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0NotEqualTo(String value) {
            addCriterion("ext_0 <>", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0GreaterThan(String value) {
            addCriterion("ext_0 >", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0GreaterThanOrEqualTo(String value) {
            addCriterion("ext_0 >=", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0LessThan(String value) {
            addCriterion("ext_0 <", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0LessThanOrEqualTo(String value) {
            addCriterion("ext_0 <=", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0Like(String value) {
            addCriterion("ext_0 like", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0NotLike(String value) {
            addCriterion("ext_0 not like", value, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0In(List<String> values) {
            addCriterion("ext_0 in", values, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0NotIn(List<String> values) {
            addCriterion("ext_0 not in", values, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0Between(String value1, String value2) {
            addCriterion("ext_0 between", value1, value2, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt0NotBetween(String value1, String value2) {
            addCriterion("ext_0 not between", value1, value2, "ext0");
            return (Criteria) this;
        }

        public Criteria andExt1IsNull() {
            addCriterion("ext_1 is null");
            return (Criteria) this;
        }

        public Criteria andExt1IsNotNull() {
            addCriterion("ext_1 is not null");
            return (Criteria) this;
        }

        public Criteria andExt1EqualTo(String value) {
            addCriterion("ext_1 =", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotEqualTo(String value) {
            addCriterion("ext_1 <>", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1GreaterThan(String value) {
            addCriterion("ext_1 >", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1GreaterThanOrEqualTo(String value) {
            addCriterion("ext_1 >=", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1LessThan(String value) {
            addCriterion("ext_1 <", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1LessThanOrEqualTo(String value) {
            addCriterion("ext_1 <=", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1Like(String value) {
            addCriterion("ext_1 like", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotLike(String value) {
            addCriterion("ext_1 not like", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1In(List<String> values) {
            addCriterion("ext_1 in", values, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotIn(List<String> values) {
            addCriterion("ext_1 not in", values, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1Between(String value1, String value2) {
            addCriterion("ext_1 between", value1, value2, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotBetween(String value1, String value2) {
            addCriterion("ext_1 not between", value1, value2, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt2IsNull() {
            addCriterion("ext_2 is null");
            return (Criteria) this;
        }

        public Criteria andExt2IsNotNull() {
            addCriterion("ext_2 is not null");
            return (Criteria) this;
        }

        public Criteria andExt2EqualTo(String value) {
            addCriterion("ext_2 =", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotEqualTo(String value) {
            addCriterion("ext_2 <>", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2GreaterThan(String value) {
            addCriterion("ext_2 >", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2GreaterThanOrEqualTo(String value) {
            addCriterion("ext_2 >=", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2LessThan(String value) {
            addCriterion("ext_2 <", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2LessThanOrEqualTo(String value) {
            addCriterion("ext_2 <=", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2Like(String value) {
            addCriterion("ext_2 like", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotLike(String value) {
            addCriterion("ext_2 not like", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2In(List<String> values) {
            addCriterion("ext_2 in", values, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotIn(List<String> values) {
            addCriterion("ext_2 not in", values, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2Between(String value1, String value2) {
            addCriterion("ext_2 between", value1, value2, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotBetween(String value1, String value2) {
            addCriterion("ext_2 not between", value1, value2, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt3IsNull() {
            addCriterion("ext_3 is null");
            return (Criteria) this;
        }

        public Criteria andExt3IsNotNull() {
            addCriterion("ext_3 is not null");
            return (Criteria) this;
        }

        public Criteria andExt3EqualTo(String value) {
            addCriterion("ext_3 =", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3NotEqualTo(String value) {
            addCriterion("ext_3 <>", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3GreaterThan(String value) {
            addCriterion("ext_3 >", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3GreaterThanOrEqualTo(String value) {
            addCriterion("ext_3 >=", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3LessThan(String value) {
            addCriterion("ext_3 <", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3LessThanOrEqualTo(String value) {
            addCriterion("ext_3 <=", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3Like(String value) {
            addCriterion("ext_3 like", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3NotLike(String value) {
            addCriterion("ext_3 not like", value, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3In(List<String> values) {
            addCriterion("ext_3 in", values, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3NotIn(List<String> values) {
            addCriterion("ext_3 not in", values, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3Between(String value1, String value2) {
            addCriterion("ext_3 between", value1, value2, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt3NotBetween(String value1, String value2) {
            addCriterion("ext_3 not between", value1, value2, "ext3");
            return (Criteria) this;
        }

        public Criteria andExt4IsNull() {
            addCriterion("ext_4 is null");
            return (Criteria) this;
        }

        public Criteria andExt4IsNotNull() {
            addCriterion("ext_4 is not null");
            return (Criteria) this;
        }

        public Criteria andExt4EqualTo(String value) {
            addCriterion("ext_4 =", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4NotEqualTo(String value) {
            addCriterion("ext_4 <>", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4GreaterThan(String value) {
            addCriterion("ext_4 >", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4GreaterThanOrEqualTo(String value) {
            addCriterion("ext_4 >=", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4LessThan(String value) {
            addCriterion("ext_4 <", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4LessThanOrEqualTo(String value) {
            addCriterion("ext_4 <=", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4Like(String value) {
            addCriterion("ext_4 like", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4NotLike(String value) {
            addCriterion("ext_4 not like", value, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4In(List<String> values) {
            addCriterion("ext_4 in", values, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4NotIn(List<String> values) {
            addCriterion("ext_4 not in", values, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4Between(String value1, String value2) {
            addCriterion("ext_4 between", value1, value2, "ext4");
            return (Criteria) this;
        }

        public Criteria andExt4NotBetween(String value1, String value2) {
            addCriterion("ext_4 not between", value1, value2, "ext4");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}