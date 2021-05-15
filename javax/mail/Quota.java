package javax.mail;

public class Quota {
    public String quotaRoot;
    public Resource[] resources;

    public static class Resource {
        public long limit;
        public String name;
        public long usage;

        public Resource(String name2, long usage2, long limit2) {
            this.name = name2;
            this.usage = usage2;
            this.limit = limit2;
        }
    }

    public Quota(String quotaRoot2) {
        this.quotaRoot = quotaRoot2;
    }

    public void setResourceLimit(String name, long limit) {
        if (this.resources == null) {
            Resource[] resourceArr = new Resource[1];
            this.resources = resourceArr;
            resourceArr[0] = new Resource(name, 0, limit);
            return;
        }
        int i = 0;
        while (true) {
            Resource[] resourceArr2 = this.resources;
            if (i >= resourceArr2.length) {
                Resource[] ra = new Resource[(resourceArr2.length + 1)];
                System.arraycopy(resourceArr2, 0, ra, 0, resourceArr2.length);
                ra[ra.length - 1] = new Resource(name, 0, limit);
                this.resources = ra;
                return;
            } else if (resourceArr2[i].name.equalsIgnoreCase(name)) {
                this.resources[i].limit = limit;
                return;
            } else {
                i++;
            }
        }
    }
}
