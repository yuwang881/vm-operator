package vmware.CPBU.utils;

import vmware.CPBU.crd.vm.VirtualMachine;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedVMEvent implements Delayed {
    private String vm;
    private long startTime;

    public DelayedVMEvent(String vm, long delayInMilliseconds) {
        this.vm = vm;
        this.startTime = System.currentTimeMillis() + delayInMilliseconds;
    }

    public String getVm() {
        return vm;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed that) {
        long result = this.getDelay(TimeUnit.MILLISECONDS)
                - that.getDelay(TimeUnit.MILLISECONDS);
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        }
        return 0;
    }
}
