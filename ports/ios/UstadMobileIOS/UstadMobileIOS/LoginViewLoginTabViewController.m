//
//  LoginViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginViewLoginTabViewController.h"
#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "java/io/InputStream.h"
#include "UstadMobileSystemImpl.h"
#include "LoginController.h"
#include "MessageIDConstants.h"
#import "LoginPageViewController.h"

@interface LoginViewLoginTabViewController ()
@property NSString *xAPIServerURL;
@property (retain, nonatomic) IBOutlet UIButton *registerButton;
- (IBAction)registerButtonClicked:(UIButton *)sender;
@end

@implementation LoginViewLoginTabViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUIStrings];
}

-(void)setUIStrings {
    ComUstadmobileCoreImplUstadMobileSystemImpl *impl = [ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
    self.usernameTextField.placeholder = [impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_username];
    self.passwordTextField.placeholder = [impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_password];
    [self.loginButton setTitle:[impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_login] forState:UIControlStateNormal];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}




- (void)setXAPIServerURLWithNSString:(NSString *)xAPIServerURL {
    self.xAPIServerURL = xAPIServerURL;
}

- (void)setVersionLabelWithNSString:(NSString *)versionLabel {
    
}

- (IBAction)loginButtonClicked:(UIButton *)sender {
    LoginPageViewController *loginViewController = (LoginPageViewController *)self.parentViewController;
    
    NSString *xapiServer = loginViewController.xapiServer;
    
    [loginViewController.loginController handleClickLoginWithNSString:self.usernameTextField.text withNSString:self.passwordTextField.text withNSString:xapiServer];
    
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/
- (IBAction)registerButtonClicked:(UIButton *)sender {
    LoginPageViewController *loginPageViewController = (LoginPageViewController *)self.parentViewController;
    [loginPageViewController showViewControllerAtIndex:1 animation:YES];
}
@end
