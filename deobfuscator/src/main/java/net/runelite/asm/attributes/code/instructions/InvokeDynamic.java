/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>, Kyle Escobar <https://github.com/kyleescobar>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.asm.attributes.code.instructions;

import java.util.Collections;
import java.util.List;
import net.runelite.asm.Field;
import net.runelite.asm.attributes.code.Instruction;
import net.runelite.asm.attributes.code.InstructionType;
import net.runelite.asm.attributes.code.Instructions;
import net.runelite.asm.attributes.code.instruction.types.GetFieldInstruction;
import net.runelite.asm.attributes.code.instruction.types.InvokeInstruction;
import net.runelite.asm.execution.Frame;
import net.runelite.asm.execution.InstructionContext;
import net.runelite.asm.execution.Stack;
import net.runelite.asm.execution.StackContext;
import net.runelite.asm.execution.Value;
import net.runelite.asm.pool.Method;
import net.runelite.asm.signature.Signature;
import net.runelite.deob.deobfuscators.mapping.MappingExecutorUtil;
import net.runelite.deob.deobfuscators.mapping.ParallelExecutorMapping;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

public class InvokeDynamic extends Instruction implements InvokeInstruction
{
	private final String name;
	private final String desc;
	private final Handle bsm;
	private final Object[] bsmArgs;

	public InvokeDynamic(Instructions instructions, String name, String desc, Handle bsm, Object[] bsmArgs)
	{
		super(instructions, InstructionType.INVOKEDYNAMIC);
		this.name = name;
		this.desc = desc;
		this.bsm = bsm;
		this.bsmArgs = bsmArgs;
	}

	@Override
	public String toString()
	{
		return "invokedynamic in " + this.getInstructions().getCode().getMethod();
	}

	@Override
	public void accept(MethodVisitor visitor)
	{
		visitor.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<net.runelite.asm.Method> getMethods()
	{
		return Collections.EMPTY_LIST;
	}

	@Override
	public InstructionContext execute(Frame frame)
	{
		InstructionContext ins = new InstructionContext(this, frame);
		Stack stack = frame.getStack();

		Signature type = new Signature(desc);
		int count = type.size();

		for (int i = 0; i < count; ++i)
		{
			StackContext arg = stack.pop();
			ins.pop(arg);
		}

		if (!type.isVoid())
		{
			StackContext ctx = new StackContext(ins,
				type.getReturnValue(),
				Value.UNKNOWN
			);
			stack.push(ctx);

			ins.push(ctx);
		}

		return ins;
	}

	@Override
	public void removeParameter(int idx)
	{
		throw new UnsupportedOperationException("invokedynamic not supported");
	}

	@Override
	public Method getMethod()
	{
		return null;
	}

	@Override
	public void map(ParallelExecutorMapping mapping, InstructionContext ctx, InstructionContext other)
	{
		int bound = ctx.getPops().size();
		for (int i = 0; i < bound; i++)
		{
			StackContext s1 = ctx.getPops().get(i);
			StackContext s2 = other.getPops().get(i);
			InstructionContext base1 = MappingExecutorUtil.resolve(s1.getPushed(), s1);
			InstructionContext base2 = MappingExecutorUtil.resolve(s2.getPushed(), s2);
			if (base1.getInstruction() instanceof GetFieldInstruction && base2.getInstruction() instanceof GetFieldInstruction)
			{
				GetFieldInstruction gf1 = (GetFieldInstruction) base1.getInstruction();
				GetFieldInstruction gf2 = (GetFieldInstruction) base2.getInstruction();

				Field f1 = gf1.getMyField();
				Field f2 = gf2.getMyField();

				if (f1 != null && f2 != null)
				{
					mapping.map(this, f1, f2);
				}
			}
		}
	}

	@Override
	public boolean isSame(InstructionContext thisIc, InstructionContext otherIc)
	{
		if (thisIc.getInstruction().getClass() != otherIc.getInstruction().getClass())
		{
			return false;
		}

		InvokeDynamic thisIi = (InvokeDynamic) thisIc.getInstruction(),
			otherIi = (InvokeDynamic) otherIc.getInstruction();

		Signature thisIiType = new Signature(thisIi.desc);
		Signature otherIiType = new Signature(otherIi.desc);

        return MappingExecutorUtil.isMaybeEqual(thisIiType, otherIiType);
    }

	@Override
	public boolean canMap(InstructionContext thisIc)
	{
		return true;
	}

	@Override
	public void setMethod(Method method)
	{
		throw new RuntimeException("Im not about to write the code to resolve the lambda's HANDLE target. " +
			"Adam didnt make any easy way to add these 'fake', Methods to the class structure for lookup after the handle" +
			"BSM args are passed. Maybe he should of just used ASM-TREE after all. :)");
	}
}