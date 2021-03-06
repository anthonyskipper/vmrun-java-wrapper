package org.skife.vmware;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.io.CharStreams;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import static org.skife.vmware.ProcessResult.run;

public class VMRun
{
    private final File vmrun;

    private VMRun(File vmrun)
    {
        this.vmrun = vmrun;
    }

    public static VMRun withExecutableAt(File executable)
    {
        return new VMRun(executable);
    }

    public static VMRun withExecutableAt(String executable)
    {
        return new VMRun(new File(executable));
    }


    public void start(File vmx, GUI gui) throws IOException, InterruptedException
    {
        run(vmrun.getAbsolutePath(), "start", vmx.getAbsolutePath(), gui.commandLineValue()).explodeOnError();
    }


    public Guest createGuest(String vmx, String guestUser, String guestPass)
    {
        return createGuest(new File(vmx), guestUser, guestPass);
    }


    public Guest createGuest(File vmx, String guestUser, String guestPass)
    {
        return new Guest(this, vmx, guestUser, guestPass);
    }

    public void stop(File vmx) throws IOException, InterruptedException
    {
        run(vmrun.getAbsolutePath(), "stop", vmx.getAbsolutePath(), "soft");
    }

    public List<File> list() throws IOException, InterruptedException
    {
        ProcessResult out = run(vmrun.getAbsolutePath(), "list").explodeOnError();

        List<String> lines = CharStreams.readLines(new InputStreamReader(new ByteArrayInputStream(out.getStdout())));
        Iterator<String> i = lines.iterator();
        Preconditions.checkState(i.hasNext(), "No header line from vmrun list execution!");
        i.next();

        return ImmutableList.copyOf(Iterators.transform(i, new Function<String, File>()
        {
            @Override
            public File apply(@Nullable String input)
            {
                return new File(input);
            }
        }));
    }

    public int runScriptInGuest(File vmx, String guestUser, String guestPass, File interpreter, String scriptBody) throws IOException, InterruptedException
    {
        return run(vmrun.getAbsolutePath(),
                                "-gu", guestUser,
                                "-gp", guestPass,
                                "runScriptInGuest",
                                vmx.getAbsolutePath(),
                                interpreter.getAbsolutePath(),
                                scriptBody).getExitCode();
    }

    public void copyFileFromGuestToHost(File vmx, String guestUser, String guestPass, File guestPath, File hostPath) throws IOException, InterruptedException
    {
        run(vmrun.getAbsolutePath(),
            "-gu", guestUser,
            "-gp", guestPass,
            "CopyFileFromGuestToHost",
            vmx.getAbsolutePath(),
            guestPath.getAbsolutePath(),
            hostPath.getAbsolutePath()).explodeOnError();
    }

    public void copyFileFromHostToGuest(File vmx, String guestUser, String guestPass, File hostPath, File guestPath) throws IOException, InterruptedException
    {
        run(vmrun.getAbsolutePath(),
            "-gu", guestUser,
            "-gp", guestPass,
            "CopyFileFromHostToGuest",
            vmx.getAbsolutePath(),
            hostPath.getAbsolutePath(),
            guestPath.getAbsolutePath()).explodeOnError();

    }
}
