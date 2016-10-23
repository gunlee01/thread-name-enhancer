/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package gunlee.proto.tne.asm.probe;

import gunlee.proto.tne.asm.IASM;
import scouter.org.objectweb.asm.*;
import scouter.org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.HashSet;

public class ServletServiceProbe implements IASM, Opcodes {
	public HashSet<String> servlets = new HashSet<String>();

	public ClassVisitor transform(ClassVisitor cv, String className) {
		return new ServletServiceCV(cv, className);
	}
}

class ServletServiceCV extends ClassVisitor implements Opcodes {
	private String className;
	public ServletServiceCV(ClassVisitor cv, String className) {
		super(ASM4, cv);
		this.className = className;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null) {
			return mv;
		}
		if (desc.startsWith("(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;")) {
			if ("service".equals(name)) {
				return new ServletServiceMV(access, desc, mv);
			}
		}
		return mv;
	}
}

/**
 * Method visitor
 *  - HttpServlet.service()
 */
class ServletServiceMV extends LocalVariablesSorter implements Opcodes {

    private Label labelTry = new Label();

	public ServletServiceMV(int access, String desc, MethodVisitor mv) {
		super(ASM4, access, desc, mv);
	}

	/**
	 * service() 호출 시작지점에서
	 * CommonDeco.beforeHttpService(Object req, Object res)
	 * 호출하는 bytecode를 추가한다
	 */
	@Override
	public void visitCode() {
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "gunlee/proto/tne/deco/CommonDeco",
				                                 "beforeHttpService", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);

        mv.visitLabel(labelTry);
        mv.visitCode();

	}

	/**
	 * service() 메소드의 return 직전에
	 * CommonDeco.afterHttpService(Object req, Object res)
	 * 호출하는 bytecode를 추가한다
	 */
	@Override
	public void visitInsn(int opcode) {
		if ((opcode >= IRETURN && opcode <= RETURN)) {
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "gunlee/proto/tne/deco/CommonDeco",
					                                 "afterHttpService", "(Ljava/lang/Throwable;)V", false);
		}
		mv.visitInsn(opcode);
	}

	/**
	 * service() 호출 exception 발생시
	 * CommonDeco.beforeHttpService(Object req, Object res)
	 * 호출하는 bytecode를 추가한다
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		Label labelCatch = new Label();
		mv.visitTryCatchBlock(labelTry, labelCatch, labelCatch, null);
		mv.visitLabel(labelCatch);
        mv.visitInsn(DUP);
        int errIdx = newLocal(Type.getType(Throwable.class));
        mv.visitVarInsn(Opcodes.ASTORE, errIdx);
        mv.visitVarInsn(Opcodes.ALOAD, errIdx);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "gunlee/proto/tne/deco/CommonDeco",
				                                 "afterHttpService", "(Ljava/lang/Throwable;)V", false);
		mv.visitInsn(ATHROW);
		mv.visitMaxs(maxStack + 3, maxLocals + 1);
	}
}
