package ca.ubc.spl.fishtail.model.parser;

import ca.ubc.spl.fishtail.TokenUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionContextManager;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.ContextUi;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.context.ui.ContextUiPlugin;

public class JdtDoiAdapter
{
  private ActivePackages packages;
  private List<String> documentTerms;
  private IInteractionElement node;
  private IJavaElement parentNode;
  private boolean shouldSearch;
  private IJavaElement jNode;
  private List<String> allTokens = new ArrayList();

  public void generatePackagesAndTokens()
  {
    this.packages = new ActivePackages();
    this.documentTerms = new ArrayList();

    for (IInteractionElement node : getActiveLandmarks()) {
      IJavaElement element = getElement(node);
      if (element != null) {
        addJavaElement(element);
        IJavaElement pkg = element.getAncestor(4);
        if (pkg != null)
          this.packages.addPackage(pkg.getElementName());
      }
    }
  }

  private static String cropFirstToken(String identifier)
  {
    List t = TokenUtil.getTokens(identifier);
    if (t.size() > 1)
    {
      t.remove(0);
    }
    String s = TokenUtil.toDelimited(t, "");
    return s;
  }

  private static String cropAtParens(String identifier) {
    int i = identifier.indexOf('(');
    if (i >= 0) {
      return identifier.substring(0, i);
    }
    return identifier;
  }

  private void addJavaElement(IJavaElement je) {
    ILabelProvider l = ContextUiPlugin.getDefault().getContextLabelProvider("java");
    String id = l.getText(je);
    if ((je instanceof IMethod))
    {
      id = cropFirstToken(cropAtParens(id));
      this.documentTerms.add(id);
    } else if (((je instanceof IField)) || ((je instanceof IType)) || 
      ((je instanceof IPackageFragmentRoot)) || ((je instanceof IPackageFragment)))
    {
      this.documentTerms.add(id);
    }
  }

  public ActivePackages getActivePackages() {
    return this.packages;
  }

  public boolean isTaskActive() {
    IInteractionContextManager mgr = ContextCore.getContextManager();
    return mgr.isContextActive();
  }

  private Collection<IInteractionElement> getActiveLandmarks() {
    IInteractionContextManager mgr = ContextCore.getContextManager();
    return mgr.getActiveContext().getInteresting();
  }

  public IJavaElement getElement(IInteractionElement node)
  {
    IJavaElement element = null;
    if ("java".equals(node.getContentType())) {
      element = JavaCore.create(node.getHandleIdentifier());
    }
    return element;
  }

  public void setElement(IInteractionElement node) {
    this.allTokens.clear();

    this.node = node;
    this.jNode = getElement(node);
    IJavaElement newParentNode = this.jNode.getAncestor(IJavaElement.TYPE);
    IJavaElement pkgNode = this.jNode.getAncestor(IJavaElement.PACKAGE_FRAGMENT);

    if (newParentNode != null) {
      this.allTokens.add(newParentNode.getElementName());
    }
    if (!newParentNode.equals(this.parentNode)) {
      if (newParentNode == this.jNode)
        this.parentNode = null;
      else {
        this.parentNode = newParentNode;
      }

      this.shouldSearch = true;
    } else {
      this.shouldSearch = false;
    }

    if (pkgNode != null)
      this.allTokens.add(pkgNode.getElementName());
    this.allTokens.add(this.jNode.getElementName());
  }

  public boolean shouldSearch()
  {
    return this.shouldSearch;
  }

  public List<String> getSearchTokens()
  {
    return this.allTokens;
  }

  public List<String> getDocumentTerms() {
    return this.documentTerms;
  }
}

/* Location:           C:\EclipseFishtail\Fishtail-Old\ca.ubc.spl.fishtail_0.1.0.200712071601.jar
 * Qualified Name:     ca.ubc.spl.fishtail.model.parser.JdtDoiAdapter
 * JD-Core Version:    0.6.0
 */